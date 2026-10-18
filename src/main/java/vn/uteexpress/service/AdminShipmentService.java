package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.User;

@Service
public class AdminShipmentService {

	private final ShipmentService shipmentService;

	public AdminShipmentService(ShipmentService shipmentService) {
		this.shipmentService = shipmentService;
	}

	@Transactional
	public Shipment assignShipper(Long shipmentId, Long shipperId) {
		return shipmentService.assignShipper(shipmentId, shipperId);
	}

	@Transactional(readOnly = true)
	public List<Shipment> getUnassignedShipments() {
		return shipmentService.getUnassignedShipments();
	}

	@Transactional(readOnly = true)
	public List<User> getShippers() {
		return shipmentService.getShippers();
	}

	@Transactional(readOnly = true)
	public Shipment getShipment(Long shipmentId) {
		return shipmentService.getById(shipmentId);
	}
}