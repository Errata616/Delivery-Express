package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class ShipperService {

	private final ShipmentRepository shipmentRepository;
	private final UserRepository userRepository;
	private final ShipmentService shipmentService;

	public ShipperService(ShipmentRepository shipmentRepository, UserRepository userRepository,
			ShipmentService shipmentService) {

		this.shipmentRepository = shipmentRepository;
		this.userRepository = userRepository;
		this.shipmentService = shipmentService;
	}

	/**
	 * Lấy Shipper và kiểm tra tài khoản.
	 */
	private User findShipper(Long shipperId) {

		User shipper = userRepository.findById(shipperId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shipper"));

		if (shipper.getRole() == null || !"SHIPPER".equalsIgnoreCase(shipper.getRole().getName())) {

			throw new IllegalArgumentException("User này không có quyền Shipper");
		}

		if (!shipper.isEnabled()) {

			throw new IllegalArgumentException("Tài khoản Shipper đang bị khóa");
		}

		return shipper;
	}

	/**
	 * Lấy toàn bộ Shipment của Shipper.
	 */
	@Transactional(readOnly = true)
	public List<Shipment> getShipments(Long shipperId) {

		findShipper(shipperId);

		return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId);
	}

	/**
	 * Lấy các Shipment đang được giao cho Shipper.
	 *
	 * Bao gồm: ASSIGNED PICKED_UP DELIVERING
	 */
	@Transactional(readOnly = true)
	public List<Shipment> getActiveShipments(Long shipperId) {

		findShipper(shipperId);

		return shipmentRepository.findByShipperIdAndStatusInOrderByCreatedAtDesc(shipperId,
				List.of(ShipmentStatus.ASSIGNED, ShipmentStatus.PICKED_UP, ShipmentStatus.DELIVERING));
	}

	/**
	 * Lấy các Shipment đã giao thành công.
	 */
	@Transactional(readOnly = true)
	public List<Shipment> getDeliveredShipments(Long shipperId) {

		findShipper(shipperId);

		return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId).stream()
				.filter(shipment -> shipment.getStatus() == ShipmentStatus.DELIVERED).toList();
	}

	/**
	 * Shipper cập nhật trạng thái Shipment.
	 *
	 * Quan trọng: Shipment phải thuộc đúng Shipper.
	 */
	@Transactional
	public Shipment updateShipmentStatus(Long shipperId, Long shipmentId, ShipmentStatus newStatus) {

		findShipper(shipperId);

		Shipment shipment = shipmentRepository.findById(shipmentId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shipment"));

		/*
		 * Kiểm tra Shipment có được giao cho đúng Shipper hay không.
		 */
		if (shipment.getShipper() == null || shipment.getShipper().getId() == null
				|| !shipment.getShipper().getId().equals(shipperId)) {

			throw new IllegalArgumentException("Shipment không thuộc Shipper này");
		}

		/*
		 * Shipper chỉ được cập nhật các trạng thái thuộc quy trình giao hàng.
		 */
		if (newStatus != ShipmentStatus.PICKED_UP && newStatus != ShipmentStatus.DELIVERING
				&& newStatus != ShipmentStatus.DELIVERED && newStatus != ShipmentStatus.FAILED) {

			throw new IllegalArgumentException("Shipper không được chuyển Shipment sang trạng thái " + newStatus);
		}

		return shipmentService.updateStatus(shipmentId, newStatus);
	}

	/**
	 * Shipper nhận Shipment.
	 *
	 * Thực tế Shipment phải được Admin/Manager assign trước, nên hàm này chỉ kiểm
	 * tra Shipment đã thuộc Shipper.
	 */
	@Transactional(readOnly = true)
	public Shipment getShipment(Long shipperId, Long shipmentId) {

		findShipper(shipperId);

		Shipment shipment = shipmentRepository.findById(shipmentId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Shipment"));

		if (shipment.getShipper() == null || !shipment.getShipper().getId().equals(shipperId)) {

			throw new IllegalArgumentException("Shipment không thuộc Shipper này");
		}

		return shipment;
	}
}