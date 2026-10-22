package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.dto.AdminUserResponse;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.AdminShipmentService;

@RestController
@RequestMapping("/api/admin/shipments")
public class AdminShipmentController {

	private final AdminShipmentService adminShipmentService;
	private final UserRepository userRepository;

	public AdminShipmentController(AdminShipmentService adminShipmentService, UserRepository userRepository) {
		this.adminShipmentService = adminShipmentService;
		this.userRepository = userRepository;
	}

	@GetMapping("/unassigned")
	public ResponseEntity<List<Shipment>> getUnassignedShipments(Authentication authentication) {
		User currentUser = getCurrentUser(authentication);
		List<Shipment> shipments = adminShipmentService.getUnassignedShipments();
		if (isAdmin(currentUser)) {
			return ResponseEntity.ok(shipments);
		}
		return ResponseEntity.ok(adminShipmentService.filterHiddenShipments(currentUser.getId(), shipments));
	}

	@GetMapping("/current")
	public ResponseEntity<List<Shipment>> getCurrentShipments(Authentication authentication) {
		User currentUser = getCurrentUser(authentication);
		List<Shipment> shipments = adminShipmentService.getCurrentShipments();
		if (isAdmin(currentUser)) {
			return ResponseEntity.ok(shipments);
		}
		List<Shipment> manageable = shipments.stream()
				.filter(shipment -> shipment.getShipper() == null
						|| (shipment.getShipper().getManager() != null
								&& currentUser.getId().equals(shipment.getShipper().getManager().getId())))
				.toList();
		return ResponseEntity.ok(adminShipmentService.filterHiddenShipments(currentUser.getId(), manageable));
	}

	@GetMapping("/shippers")
	public ResponseEntity<List<AdminUserResponse>> getShippers(Authentication authentication) {
		User currentUser = getCurrentUser(authentication);
		List<User> shippers = isAdmin(currentUser)
				? adminShipmentService.getShippers()
				: userRepository.findByManager_Id(currentUser.getId());
		return ResponseEntity.ok(shippers.stream().map(AdminUserResponse::from).toList());
	}

	@PutMapping("/{shipmentId}/assign/{shipperId}")
	public ResponseEntity<Shipment> assignShipper(@PathVariable Long shipmentId, @PathVariable Long shipperId,
			Authentication authentication) {
		return ResponseEntity.ok(adminShipmentService.assignShipper(shipmentId, shipperId,
				getCurrentUser(authentication)));
	}

	@GetMapping("/{shipmentId}")
	public ResponseEntity<Shipment> getShipment(@PathVariable Long shipmentId) {

		return ResponseEntity.ok(adminShipmentService.getShipment(shipmentId));
	}

	@GetMapping("/deletable")
	public ResponseEntity<List<Shipment>> getDeletableShipments(Authentication authentication) {
		requireAdmin(getCurrentUser(authentication));
		return ResponseEntity.ok(adminShipmentService.getDeletableShipments());
	}

	@DeleteMapping("/{shipmentId}")
	public ResponseEntity<Void> deleteCompletedShipment(@PathVariable Long shipmentId,
			Authentication authentication) {
		requireAdmin(getCurrentUser(authentication));
		adminShipmentService.deleteCompletedShipment(shipmentId);
		return ResponseEntity.noContent().build();
	}

	private User getCurrentUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalArgumentException("Chưa đăng nhập");
		}
		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}

	private boolean isAdmin(User user) {
		return user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
	}

	private void requireAdmin(User user) {
		if (!isAdmin(user)) {
			throw new IllegalArgumentException("Chỉ Admin được phép xóa vĩnh viễn vận đơn");
		}
	}
}