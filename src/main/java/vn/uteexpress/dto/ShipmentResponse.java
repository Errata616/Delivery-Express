package vn.uteexpress.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import vn.uteexpress.entity.Address;
import vn.uteexpress.entity.Shipment;

public record ShipmentResponse(Long id, String trackingCode, String status, String serviceType, Long orderId,
		Long customerId, ShipperResponse shipper, HubResponse originHub, HubResponse destinationHub,
		String senderName, String senderPhone, String pickupAddress, String pickupDistrict, String pickupProvince,
		String receiverName, String receiverPhone, String deliveryAddress, String deliveryDistrict,
		String deliveryProvince, String itemDescription, BigDecimal weightKg, Integer lengthCm, Integer widthCm,
		Integer heightCm, BigDecimal codAmount, BigDecimal shippingFee, LocalDateTime createdAt,
		LocalDateTime assignedAt, LocalDateTime pickedUpAt, LocalDateTime deliveredAt, String note) {

	public static ShipmentResponse from(Shipment s) {

		String receiverName = s.getReceiverName();
		String receiverPhone = s.getReceiverPhone();
		String deliveryAddress = s.getDeliveryAddress();
		String deliveryDistrict = s.getDeliveryDistrict();
		String deliveryProvince = s.getDeliveryProvince();

		// Legacy shipments created from a shop order: read the receiver from the order's address
		if (receiverName == null && s.getOrder() != null && s.getOrder().getShippingAddress() != null) {
			Address a = s.getOrder().getShippingAddress();
			receiverName = a.getReceiverName();
			receiverPhone = a.getPhone();
			deliveryAddress = a.getAddress();
			deliveryDistrict = a.getDistrict();
			deliveryProvince = a.getCity();
		}

		Long customerId = s.getCustomer() != null ? s.getCustomer().getId()
				: (s.getOrder() != null && s.getOrder().getUser() != null ? s.getOrder().getUser().getId() : null);

		return new ShipmentResponse(s.getId(), s.getTrackingCode(), s.getStatus().name(),
				s.getServiceType() != null ? s.getServiceType().name() : null,
				s.getOrder() != null ? s.getOrder().getId() : null, customerId,
				s.getShipper() != null ? ShipperResponse.from(s.getShipper()) : null,
				s.getOriginHub() != null ? HubResponse.from(s.getOriginHub()) : null,
				s.getDestinationHub() != null ? HubResponse.from(s.getDestinationHub()) : null, s.getSenderName(),
				s.getSenderPhone(), s.getPickupAddress(), s.getPickupDistrict(), s.getPickupProvince(), receiverName,
				receiverPhone, deliveryAddress, deliveryDistrict, deliveryProvince, s.getItemDescription(),
				s.getWeightKg(), s.getLengthCm(), s.getWidthCm(), s.getHeightCm(), s.getCodAmount(),
				s.getShippingFee(), s.getCreatedAt(), s.getAssignedAt(), s.getPickedUpAt(), s.getDeliveredAt(),
				s.getNote());
	}
}