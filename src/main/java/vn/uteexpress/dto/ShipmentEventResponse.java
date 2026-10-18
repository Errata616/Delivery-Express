package vn.uteexpress.dto;

import java.time.LocalDateTime;

import vn.uteexpress.entity.ShipmentEvent;

public record ShipmentEventResponse(Long id, String status, String hubCode, String hubName, String note,
		String updatedBy, LocalDateTime createdAt) {

	public static ShipmentEventResponse from(ShipmentEvent e) {
		return new ShipmentEventResponse(e.getId(), e.getStatus().name(),
				e.getHub() != null ? e.getHub().getCode() : null, e.getHub() != null ? e.getHub().getName() : null,
				e.getNote(), e.getUpdatedBy() != null ? e.getUpdatedBy().getFullName() : null, e.getCreatedAt());
	}
}