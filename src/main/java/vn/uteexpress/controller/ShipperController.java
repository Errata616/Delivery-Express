package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.ShipperService;

@RestController
@RequestMapping("/api/shipper")
public class ShipperController {

	private final ShipperService shipperService;
	private final UserRepository userRepository;

	public ShipperController(ShipperService shipperService, UserRepository userRepository) {

		this.shipperService = shipperService;
		this.userRepository = userRepository;
	}

	/**
	 * Kiểm tra quyền truy cập Shipper.
	 *
	 * SHIPPER: Chỉ được xem/cập nhật Shipment của chính mình.
	 *
	 * ADMIN: Có thể truy cập Shipment của Shipper khác.
	 */
	private void checkAccess(Long shipperId, Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

		/*
		 * ADMIN được phép quản lý Shipper.
		 */
		if (isAdmin) {
			return;
		}

		/*
		 * User hiện tại.
		 */
		String username = authentication.getName();

		User currentUser = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản đang đăng nhập"));

		/*
		 * Phải là SHIPPER.
		 */
		if (currentUser.getRole() == null || !"SHIPPER".equalsIgnoreCase(currentUser.getRole().getName())) {

			throw new IllegalArgumentException("Tài khoản không có quyền Shipper");
		}

		/*
		 * Chỉ được thao tác với chính mình.
		 */
		if (!currentUser.getId().equals(shipperId)) {

			throw new IllegalArgumentException("Không được truy cập dữ liệu của Shipper khác");
		}
	}

	/**
	 * Lấy tất cả Shipment của Shipper.
	 */
	@GetMapping("/me/shipments")
	public ResponseEntity<List<Shipment>> getMyShipments(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		User currentUser = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản đang đăng nhập"));
		if (currentUser.getRole() == null || !"SHIPPER".equalsIgnoreCase(currentUser.getRole().getName())) {
			throw new IllegalArgumentException("Tài khoản không có quyền Shipper");
		}

		return ResponseEntity.ok(shipperService.getShipments(currentUser.getId()));
	}

	/**
	 * Lấy tất cả Shipment của Shipper theo ID.
	 */
	@GetMapping("/{shipperId}/shipments")
	public ResponseEntity<List<Shipment>> getShipments(@PathVariable Long shipperId, Authentication authentication) {

		checkAccess(shipperId, authentication);

		return ResponseEntity.ok(shipperService.getShipments(shipperId));
	}

	/**
	 * Lấy các Shipment đang xử lý.
	 */
	@GetMapping("/{shipperId}/shipments/active")
	public ResponseEntity<List<Shipment>> getActiveShipments(@PathVariable Long shipperId,
			Authentication authentication) {

		checkAccess(shipperId, authentication);

		return ResponseEntity.ok(shipperService.getActiveShipments(shipperId));
	}

	/**
	 * Lấy các Shipment đã giao.
	 */
	@GetMapping("/{shipperId}/shipments/delivered")
	public ResponseEntity<List<Shipment>> getDeliveredShipments(@PathVariable Long shipperId,
			Authentication authentication) {

		checkAccess(shipperId, authentication);

		return ResponseEntity.ok(shipperService.getDeliveredShipments(shipperId));
	}

	/**
	 * Xem chi tiết một Shipment.
	 */
	@GetMapping("/{shipperId}/shipments/{shipmentId}")
	public ResponseEntity<Shipment> getShipment(@PathVariable Long shipperId, @PathVariable Long shipmentId,
			Authentication authentication) {

		checkAccess(shipperId, authentication);

		return ResponseEntity.ok(shipperService.getShipment(shipperId, shipmentId));
	}

	/**
	 * Shipper cập nhật trạng thái.
	 */
	@PutMapping("/{shipperId}/shipments/{shipmentId}/status")
	public ResponseEntity<Shipment> updateStatus(@PathVariable Long shipperId, @PathVariable Long shipmentId,
			@RequestParam ShipmentStatus status, Authentication authentication) {

		checkAccess(shipperId, authentication);

		return ResponseEntity.ok(shipperService.updateShipmentStatus(shipperId, shipmentId, status));
	}
}