package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.DeletedShipmentRevenue;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.DeletedShipmentRevenueRepository;

@Service
public class AdminShipmentService {

	private final ShipmentService shipmentService;
	private final DeletedShipmentRevenueRepository deletedShipmentRevenueRepository;

	public AdminShipmentService(ShipmentService shipmentService,
			DeletedShipmentRevenueRepository deletedShipmentRevenueRepository) {
		this.shipmentService = shipmentService;
		this.deletedShipmentRevenueRepository = deletedShipmentRevenueRepository;
	}

	@Transactional
	public Shipment assignShipper(Long shipmentId, Long shipperId) {
		return shipmentService.assignShipper(shipmentId, shipperId);
	}

	@Transactional
	public Shipment assignShipper(Long shipmentId, Long shipperId, User actor) {
		return shipmentService.assignShipper(shipmentId, shipperId, actor);
	}

	@Transactional(readOnly = true)
	public List<Shipment> getUnassignedShipments() {
		return shipmentService.getUnassignedShipments();
	}

	@Transactional(readOnly = true)
	public List<Shipment> getCurrentShipments() {
		return shipmentService.getCurrentShipments();
	}

	@Transactional(readOnly = true)
	public List<Shipment> filterHiddenShipments(Long userId, List<Shipment> shipments) {
		return shipmentService.filterHiddenShipments(userId, shipments);
	}

	@Transactional(readOnly = true)
	public List<Shipment> getDeletableShipments() {
		return shipmentService.getDeletableShipments();
	}

	@Transactional
	public void deleteCompletedShipment(Long shipmentId) {
		Shipment shipment = shipmentService.getById(shipmentId);
		if (shipment.getStatus() == ShipmentStatus.DELIVERED && shipment.getShippingFee() != null) {
			Long managerId = shipment.getShipper() != null && shipment.getShipper().getManager() != null
					? shipment.getShipper().getManager().getId()
					: null;
			deletedShipmentRevenueRepository
					.save(new DeletedShipmentRevenue(shipment.getShippingFee(), managerId));
		}
		shipmentService.deleteCompletedShipment(shipmentId);
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