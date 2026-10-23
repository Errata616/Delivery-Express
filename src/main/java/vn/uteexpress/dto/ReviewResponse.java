package vn.uteexpress.dto;

import java.time.LocalDateTime;

import vn.uteexpress.entity.Review;

public record ReviewResponse(Long id, Long shipmentId, String trackingCode, String customerName, Long shipperId,
		int rating, String comment, LocalDateTime createdAt) {

	public static ReviewResponse from(Review r) {
		return new ReviewResponse(r.getId(), r.getShipment().getId(), r.getShipment().getTrackingCode(),
				TrackingResponse.maskName(r.getCustomer().getFullName()),
				r.getShipper() != null ? r.getShipper().getId() : null, r.getRating(), r.getComment(),
				r.getCreatedAt());
	}
}