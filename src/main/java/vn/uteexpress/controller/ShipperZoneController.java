package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.dto.ShipperSuggestion;
import vn.uteexpress.dto.ShipperZoneResponse;
import vn.uteexpress.dto.ShipperZoneRequest;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.ShipperZoneService;

@RestController
@RequestMapping("/api/admin/shipments")
public class ShipperZoneController {

	private final ShipperZoneService zoneService;
	private final UserRepository userRepository;

	public ShipperZoneController(ShipperZoneService zoneService, UserRepository userRepository) {
		this.zoneService = zoneService;
		this.userRepository = userRepository;
	}

	@GetMapping("/shippers/zones")
	public ResponseEntity<ApiResponse<List<ShipperZoneResponse>>> listZones(Authentication authentication) {
		return ResponseEntity.ok(ApiResponse.ok(zoneService.listAll(getCurrentUser(authentication))));
	}

	@PutMapping("/shippers/{shipperId}/zones")
	public ResponseEntity<ApiResponse<ShipperZoneResponse>> replaceZones(@PathVariable Long shipperId,
			@RequestBody ShipperZoneRequest request, Authentication authentication) {
		return ResponseEntity.ok(ApiResponse.ok(zoneService.replaceZones(shipperId, request.districtCodes(),
				getCurrentUser(authentication))));
	}

	@GetMapping("/{shipmentId}/suggested-shippers")
	public ResponseEntity<ApiResponse<List<ShipperSuggestion>>> suggest(@PathVariable Long shipmentId,
			Authentication authentication) {
		return ResponseEntity.ok(ApiResponse.ok(zoneService.suggest(shipmentId, getCurrentUser(authentication))));
	}

	private User getCurrentUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalArgumentException("Chưa đăng nhập");
		}
		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}
}