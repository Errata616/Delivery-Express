package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.dto.AdminUserResponse;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.service.AdminShipmentService;

@RestController
@RequestMapping("/api/admin/shipments")
public class AdminShipmentController {

	private final AdminShipmentService adminShipmentService;

	public AdminShipmentController(AdminShipmentService adminShipmentService) {
		this.adminShipmentService = adminShipmentService;
	}

	@GetMapping("/unassigned")
	public ResponseEntity<List<Shipment>> getUnassignedShipments() {

		return ResponseEntity.ok(adminShipmentService.getUnassignedShipments());
	}

	@GetMapping("/shippers")
	public ResponseEntity<List<AdminUserResponse>> getShippers() {

		return ResponseEntity.ok(adminShipmentService.getShippers().stream().map(AdminUserResponse::from).toList());
	}

	@PutMapping("/{shipmentId}/assign/{shipperId}")
	public ResponseEntity<Shipment> assignShipper(@PathVariable Long shipmentId, @PathVariable Long shipperId) {

		return ResponseEntity.ok(adminShipmentService.assignShipper(shipmentId, shipperId));
	}

	@GetMapping("/{shipmentId}")
	public ResponseEntity<Shipment> getShipment(@PathVariable Long shipmentId) {

		return ResponseEntity.ok(adminShipmentService.getShipment(shipmentId));
	}
}