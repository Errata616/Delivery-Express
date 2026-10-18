package vn.uteexpress.dto;

import java.time.LocalDateTime;

import vn.uteexpress.entity.Notification;

public record NotificationDto(Long id, String title, String body, String type, Long shipmentId, boolean seen,
		LocalDateTime createdAt) {

	public static NotificationDto from(Notification n) {
		return new NotificationDto(n.getId(), n.getTitle(), n.getBody(), n.getType().name(), n.getShipmentId(),
				n.isSeen(), n.getCreatedAt());
	}
}