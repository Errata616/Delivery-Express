package vn.uteexpress.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.uteexpress.dto.ShipperSuggestion;
import vn.uteexpress.dto.ShipperZoneResponse;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipperZone;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.ShipperZoneRepository;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.routing.DistrictGraph;
import vn.uteexpress.routing.DistrictGraph.District;

@Service
public class ShipperZoneService {

	private static final String SHIPPER_ROLE = "SHIPPER";

	private final ShipperZoneRepository zoneRepository;
	private final UserRepository userRepository;
	private final ShipmentRepository shipmentRepository;
	private final RouteService routeService;

	public ShipperZoneService(ShipperZoneRepository zoneRepository, UserRepository userRepository,
			ShipmentRepository shipmentRepository, RouteService routeService) {
		this.zoneRepository = zoneRepository;
		this.userRepository = userRepository;
		this.shipmentRepository = shipmentRepository;
		this.routeService = routeService;
	}

	@Transactional(readOnly = true)
	public List<ShipperZoneResponse> listAll(User actor) {
		List<User> shippers = manageableShippers(actor);
		Map<Long, List<String>> zones = zonesByShipper(shippers);
		return shippers.stream().map(s -> toResponse(s, zones.getOrDefault(s.getId(), List.of()))).toList();
	}

	@Transactional
	public ShipperZoneResponse replaceZones(Long shipperId, List<String> requestedCodes, User actor) {
		User shipper = userRepository.findById(shipperId)
				.filter(u -> u.getRole() != null && SHIPPER_ROLE.equals(u.getRole().getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy shipper"));
		if (!isAdmin(actor) && !manages(actor, shipper)) {
			throw new AccessDeniedException("Managers can only manage zones for shippers in their team");
		}

		Set<String> wanted = new LinkedHashSet<>();
		if (requestedCodes != null) {
			for (String text : requestedCodes) {
				District district = routeService.resolve(text).orElseThrow(
						() -> new IllegalArgumentException("Không nhận diện được quận/huyện: " + text));
				wanted.add(district.code());
			}
		}

		List<ShipperZone> existing = zoneRepository.findByShipperId(shipperId);
		Set<String> have = existing.stream().map(ShipperZone::getDistrictCode).collect(Collectors.toSet());

		// Hibernate runs inserts before deletes, so only touch rows that actually changed
		// (otherwise re-adding a kept district would hit the unique constraint).
		zoneRepository.deleteAll(existing.stream().filter(z -> !wanted.contains(z.getDistrictCode())).toList());
		zoneRepository.saveAll(wanted.stream().filter(code -> !have.contains(code))
				.map(code -> new ShipperZone(shipper, code)).toList());

		List<String> sorted = new ArrayList<>(wanted);
		Collections.sort(sorted);
		return toResponse(shipper, sorted);
	}

	@Transactional(readOnly = true)
	public List<ShipperSuggestion> suggest(Long shipmentId, User actor) {
		Shipment shipment = shipmentRepository.findById(shipmentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vận đơn"));

		// One shipper both collects and delivers, so we match on the PICKUP district.
		// To match on the receiver's side instead, use getDeliveryProvince()/getDeliveryDistrict().
		Optional<District> target = isHcm(shipment.getPickupProvince())
				? routeService.resolve(shipment.getPickupDistrict())
				: Optional.empty();

		List<User> shippers = manageableShippers(actor).stream()
				.filter(User::isEnabled).filter(User::isAvailable).toList();
		Map<Long, List<String>> zones = zonesByShipper(shippers);

		List<ShipperSuggestion> result = new ArrayList<>();
		for (User shipper : shippers) {
			List<String> codes = zones.getOrDefault(shipper.getId(), List.of());
			String match;
			Integer hops = null;

			if (target.isEmpty()) {
				match = "UNKNOWN";
			} else if (codes.isEmpty()) {
				match = "NO_ZONE";
			} else if (codes.contains(target.get().code())) {
				match = "EXACT";
				hops = 0;
			} else {
				hops = nearest(target.get(), codes);
				match = hops == null ? "NO_ZONE" : "NEARBY";
			}
			result.add(new ShipperSuggestion(shipper.getId(), shipper.getFullName(), shipper.getPhone(), match,
					hops, codes));
		}

		result.sort(Comparator.comparingInt((ShipperSuggestion s) -> rank(s.match()))
				.thenComparingInt(s -> s.hops() == null ? Integer.MAX_VALUE : s.hops())
				.thenComparing(s -> s.fullName() == null ? "" : s.fullName()));
		return result;
	}

	// ---------- helpers ----------

	private List<User> manageableShippers(User actor) {
		if (isAdmin(actor)) {
			return userRepository.findByRoleName(SHIPPER_ROLE);
		}
		if (actor != null && actor.getRole() != null
				&& "MANAGER".equalsIgnoreCase(actor.getRole().getName())) {
			return userRepository.findByManager_Id(actor.getId()).stream()
					.filter(shipper -> shipper.getRole() != null
							&& SHIPPER_ROLE.equalsIgnoreCase(shipper.getRole().getName()))
					.toList();
		}
		throw new AccessDeniedException("Only admins and managers can manage shippers");
	}

	private static boolean manages(User manager, User shipper) {
		return manager != null && manager.getId() != null && shipper.getManager() != null
				&& manager.getId().equals(shipper.getManager().getId());
	}

	private static boolean isAdmin(User user) {
		return user != null && user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
	}

	private Integer nearest(District target, List<String> codes) {
		Integer best = null;
		for (String code : codes) {
			Optional<District> zone = routeService.resolve(code);
			if (zone.isEmpty()) {
				continue;
			}
			Optional<Integer> hops = routeService.hops(target, zone.get());
			if (hops.isPresent() && (best == null || hops.get() < best)) {
				best = hops.get();
			}
		}
		return best;
	}

	private static int rank(String match) {
		return switch (match) {
		case "EXACT" -> 0;
		case "NEARBY" -> 1;
		case "NO_ZONE" -> 2;
		default -> 3;
		};
	}

	// The district graph only covers Ho Chi Minh City.
	private static boolean isHcm(String province) {
		String p = DistrictGraph.normalize(province);
		return p.contains("ho chi minh") || p.contains("hcm") || p.contains("sai gon");
	}

	private Map<Long, List<String>> zonesByShipper(List<User> shippers) {
		if (shippers.isEmpty()) {
			return Map.of();
		}
		List<Long> ids = shippers.stream().map(User::getId).toList();
		Map<Long, List<String>> map = new HashMap<>();
		for (ShipperZone zone : zoneRepository.findByShipperIdIn(ids)) {
			map.computeIfAbsent(zone.getShipper().getId(), k -> new ArrayList<>()).add(zone.getDistrictCode());
		}
		map.values().forEach(list -> Collections.sort(list));
		return map;
	}

	private static ShipperZoneResponse toResponse(User shipper, List<String> codes) {
		return new ShipperZoneResponse(shipper.getId(), shipper.getFullName(), shipper.getPhone(),
				shipper.isEnabled(), shipper.isAvailable(), codes);
	}
}