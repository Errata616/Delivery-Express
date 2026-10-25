package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.ShipmentService;

import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import vn.uteexpress.dto.AdminUserResponse;
import vn.uteexpress.dto.BookingRequest;
import vn.uteexpress.dto.FeeQuoteRequest;
import vn.uteexpress.dto.TrackingResponse;
import vn.uteexpress.service.ShippingFeeService;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

	private final ShipmentService shipmentService;
	private final UserRepository userRepository;

	public ShipmentController(ShipmentService shipmentService, UserRepository userRepository) {

		this.shipmentService = shipmentService;
		this.userRepository = userRepository;
	}

	// =========================================================
	// ASSIGN SHIPPER
	// ADMIN / MANAGER ONLY
	// =========================================================

	@PutMapping("/{shipmentId}/assign/{shipperId}")
	public ResponseEntity<Shipment> assignShipper(@PathVariable Long shipmentId, @PathVariable Long shipperId,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		return ResponseEntity.ok(shipmentService.assignShipper(shipmentId, shipperId, currentUser));
	}

	// =========================================================
	// UPDATE SHIPMENT STATUS
	// SHIPPER OWNER / ADMIN
	// =========================================================

	@PutMapping("/{shipmentId}/status")
	public ResponseEntity<Shipment> updateStatus(@PathVariable Long shipmentId, @RequestParam ShipmentStatus status,
			@RequestParam(required = false) String note, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkCanUpdateShipmentStatus(shipmentId, currentUser);

		return ResponseEntity.ok(shipmentService.updateStatus(shipmentId, status, note));
	}

	@PutMapping("/{shipmentId}/district")
	public ResponseEntity<Shipment> updateDistrict(@PathVariable Long shipmentId, @RequestParam String district,
			Authentication authentication) {
		User currentUser = getCurrentUser(authentication);
		checkCanUpdateShipmentStatus(shipmentId, currentUser);
		Shipment shipment = shipmentService.getById(shipmentId);
		if (shipment.getShipper() == null) {
			throw new IllegalArgumentException("Vận đơn chưa được phân công cho shipper");
		}
		return ResponseEntity.ok(shipmentService.updateShipmentDistrict(
				shipment.getShipper().getId(), shipmentId, district));
	}

	// =========================================================
	// GET SHIPMENT
	// =========================================================

	@GetMapping("/{shipmentId}")
	public ResponseEntity<Shipment> getShipment(@PathVariable Long shipmentId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		Shipment shipment = shipmentService.getById(shipmentId);

		checkCanViewShipment(shipment, currentUser);

		return ResponseEntity.ok(shipment);
	}

	// =========================================================
	// GET SHIPMENTS BY ORDER
	// =========================================================

	@GetMapping("/order/{orderId}")
	public ResponseEntity<List<Shipment>> getByOrder(@PathVariable Long orderId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		List<Shipment> shipments = shipmentService.getByOrderId(orderId);

		return ResponseEntity.ok(filterShipmentsForUser(shipments, currentUser));
	}

	// =========================================================
	// PUBLIC TRACKING
	// =========================================================

	@GetMapping("/tracking/{trackingCode}")
	public ResponseEntity<TrackingResponse> getByTrackingCode(@PathVariable String trackingCode) {

		return ResponseEntity.ok(shipmentService.getTracking(trackingCode));
	}

	// =========================================================
	// CREATE BOOKING (any logged-in user)
	// =========================================================

	@PostMapping("/booking")
	public ResponseEntity<Shipment> createBooking(@Valid @RequestBody BookingRequest request,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(shipmentService.createBooking(currentUser.getId(), request));
	}

	// =========================================================
	// FEE QUOTE (price preview, nothing is saved)
	// =========================================================

	@PostMapping("/quote")
	public ResponseEntity<ShippingFeeService.FeeQuote> quote(@Valid @RequestBody BookingRequest request) {

		return ResponseEntity.ok(shipmentService.quote(request));
	}

	@PostMapping("/public-quote")
	public ResponseEntity<ShippingFeeService.FeeQuote> publicQuote(@Valid @RequestBody FeeQuoteRequest request) {

		return ResponseEntity.ok(shipmentService.quote(request));
	}

	// =========================================================
	// MY SHIPMENTS
	// =========================================================

	@GetMapping("/my")
	public ResponseEntity<List<Shipment>> getMyShipments(Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		return ResponseEntity.ok(shipmentService.getMyShipments(currentUser.getId()));
	}

	@DeleteMapping("/{shipmentId}/view")
	public ResponseEntity<Void> hideShipmentFromView(@PathVariable Long shipmentId,
			Authentication authentication) {

		User currentUser = getCurrentUser(authentication);
		shipmentService.hideShipmentFromView(shipmentId, currentUser);

		return ResponseEntity.noContent().build();
	}

	// =========================================================
	// GET SHIPMENTS BY SHIPPER
	// =========================================================

	@GetMapping("/shipper/{shipperId}")
	public ResponseEntity<List<Shipment>> getByShipper(@PathVariable Long shipperId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkShipperAccess(shipperId, currentUser);

		return ResponseEntity.ok(shipmentService.getShipmentsByShipper(shipperId));
	}

	// =========================================================
	// GET SHIPPERS
	// ADMIN / MANAGER
	// =========================================================

	@GetMapping("/shippers")
	public ResponseEntity<List<AdminUserResponse>> getShippers(Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		List<User> shippers = isAdmin(currentUser) ? shipmentService.getShippers()
				: userRepository.findByManager_Id(currentUser.getId());
		return ResponseEntity.ok(shippers.stream().map(AdminUserResponse::from).toList());
	}

	// =========================================================
	// GET UNASSIGNED
	// ADMIN / MANAGER
	// =========================================================

	@GetMapping("/unassigned")
	public ResponseEntity<List<Shipment>> getUnassigned(Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		checkManagerOrAdmin(currentUser);

		return ResponseEntity.ok(shipmentService.getUnassignedShipments());
	}

	// =========================================================
	// CURRENT USER
	// =========================================================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}

	// =========================================================
	// ROLE CHECK
	// =========================================================

	private boolean isAdmin(User user) {

		return user != null && user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
	}

	private boolean isManager(User user) {

		return user != null && user.getRole() != null && "MANAGER".equalsIgnoreCase(user.getRole().getName());
	}

	private boolean isShipper(User user) {

		return user != null && user.getRole() != null && "SHIPPER".equalsIgnoreCase(user.getRole().getName());
	}

	// =========================================================
	// MANAGER / ADMIN
	// =========================================================

	private void checkManagerOrAdmin(User currentUser) {

		if (!isManager(currentUser) && !isAdmin(currentUser)) {

			throw new IllegalArgumentException("Chỉ Manager hoặc Admin được phép thực hiện thao tác này");
		}
	}

	// =========================================================
	// SHIPPER ACCESS
	// =========================================================

	private void checkShipperAccess(Long shipperId, User currentUser) {

		if (isAdmin(currentUser)) {

			return;
		}

		if (isManager(currentUser)) {
			boolean managesShipper = userRepository.findByManager_Id(currentUser.getId()).stream()
					.anyMatch(shipper -> shipper.getId().equals(shipperId));
			if (managesShipper) {
				return;
			}
			throw new IllegalArgumentException("Manager chỉ được xem shipper thuộc đội mình");
		}

		if (!isShipper(currentUser) || !currentUser.getId().equals(shipperId)) {

			throw new IllegalArgumentException("Không được xem Shipment của Shipper khác");
		}
	}

	// =========================================================
	// SHIPMENT STATUS ACCESS
	// =========================================================

	private void checkCanUpdateShipmentStatus(Long shipmentId, User currentUser) {

		/*
		 * Admin được xử lý.
		 */
		if (isAdmin(currentUser)) {
			return;
		}

		/*
		 * Manager không trực tiếp đổi trạng thái giao hàng.
		 */
		if (isManager(currentUser)) {

			throw new IllegalArgumentException("Manager không được trực tiếp cập nhật trạng thái Shipment");
		}

		/*
		 * Chỉ Shipper được cập nhật.
		 */
		if (!isShipper(currentUser)) {

			throw new IllegalArgumentException("Chỉ Shipper hoặc Admin được cập nhật trạng thái Shipment");
		}

		Shipment shipment = shipmentService.getById(shipmentId);

		if (shipment.getShipper() == null || !shipment.getShipper().getId().equals(currentUser.getId())) {

			throw new IllegalArgumentException("Shipment không thuộc Shipper hiện tại");
		}
	}

	// =========================================================
	// VIEW SINGLE SHIPMENT
	// =========================================================

	private void checkCanViewShipment(Shipment shipment, User currentUser) {

		if (isAdmin(currentUser) || isManager(currentUser)) {

			return;
		}

		/*
		 * Shipper chỉ xem Shipment của mình.
		 */
		if (isShipper(currentUser)) {

			if (shipment.getShipper() == null || !shipment.getShipper().getId().equals(currentUser.getId())) {

				throw new IllegalArgumentException("Không được xem Shipment của Shipper khác");
			}

			return;
		}

		if (isBookingOwner(shipment, currentUser)) {
			return;
		}

		throw new IllegalArgumentException("Không có quyền xem Shipment");
	}

	// =========================================================
	// FILTER SHIPMENTS BY ORDER
	// =========================================================

	private List<Shipment> filterShipmentsForUser(List<Shipment> shipments, User currentUser) {

		if (shipments == null || shipments.isEmpty()) {

			return shipments;
		}

		/*
		 * Admin / Manager được xem toàn bộ.
		 */
		if (isAdmin(currentUser) || isManager(currentUser)) {

			return shipments;
		}

		/*
		 * Shipper chỉ nhận Shipment của chính mình.
		 */
		if (isShipper(currentUser)) {

			List<Shipment> result = shipments.stream().filter(shipment -> shipment.getShipper() != null
					&& shipment.getShipper().getId().equals(currentUser.getId())).toList();

			if (result.isEmpty()) {

				throw new IllegalArgumentException("Không được xem Shipment của Order này");
			}

			return result;
		}

		List<Shipment> ownedShipments = shipments.stream()
				.filter(shipment -> isBookingOwner(shipment, currentUser)).toList();
		if (!ownedShipments.isEmpty()) {
			return ownedShipments;
		}

		throw new IllegalArgumentException("Không có quyền xem Shipment");
	}

	private boolean isBookingOwner(Shipment shipment, User user) {

		if (shipment.getCustomer() != null && shipment.getCustomer().getId().equals(user.getId())) {
			return true;
		}

		return shipment.getOrder() != null && shipment.getOrder().getUser() != null
				&& shipment.getOrder().getUser().getId().equals(user.getId());
	}
}