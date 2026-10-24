package vn.uteexpress.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Public tracking result. Returned by an endpoint that needs no login, so it
 * only holds data that is safe to show to anyone who has the tracking code:
 * no user ids, no emails, no street addresses, and masked sender/receiver details.
 */
public class TrackingResponse {

	private String trackingCode;

	// Enum name, e.g. "IN_TRANSIT". Kept as String so the response does not
	// break when ShipmentStatus gets new values.
	private String status;

	// Human readable status, e.g. "Đang vận chuyển"
	private String statusText;

	// Masked for privacy, see maskName / maskPhone below
	private String senderName;
	private String senderPhone;
	private String receiverName;
	private String receiverPhone;

	// Area only, never the full street address
	private String pickupProvince;
	private String deliveryDistrict;
	private String deliveryProvince;

	private BigDecimal weightKg;
	private BigDecimal codAmount;
	private String itemDescription;

	// Shown once a shipper is assigned. Name only, no phone number.
	private String shipperName;

	private LocalDateTime createdAt;
	private LocalDateTime deliveredAt;

	private String pickupDistrict;

	public String getPickupDistrict() {
		return pickupDistrict;
	}

	public void setPickupDistrict(String pickupDistrict) {
		this.pickupDistrict = pickupDistrict;
	}

	// Newest first
	private List<TrackingEvent> events = new ArrayList<>();

	public TrackingResponse() {
	}

	// =========================
	// ONE STEP OF THE TIMELINE
	// =========================

	public static class TrackingEvent {

		private String status;
		private String statusText;

		// Hub name or area where it happened, may be null
		private String location;

		private String note;
		private LocalDateTime time;

		public TrackingEvent() {
		}

		public TrackingEvent(String status, String statusText, String location, String note, LocalDateTime time) {
			this.status = status;
			this.statusText = statusText;
			this.location = location;
			this.note = note;
			this.time = time;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getStatusText() {
			return statusText;
		}

		public void setStatusText(String statusText) {
			this.statusText = statusText;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getNote() {
			return note;
		}

		public void setNote(String note) {
			this.note = note;
		}

		public LocalDateTime getTime() {
			return time;
		}

		public void setTime(LocalDateTime time) {
			this.time = time;
		}
	}

	// =========================
	// MASKING HELPERS
	// =========================

	// "Nguyễn Văn An" -> "N*** V*** A***"
	public static String maskName(String name) {

		if (name == null || name.isBlank()) {
			return null;
		}

		StringBuilder masked = new StringBuilder();

		for (String word : name.trim().split("\\s+")) {
			if (masked.length() > 0) {
				masked.append(' ');
			}
			masked.append(word.charAt(0)).append("***");
		}

		return masked.toString();
	}

	// "0901234567" -> "090****567"
	public static String maskPhone(String phone) {

		if (phone == null || phone.length() < 7) {
			return phone == null ? null : "***";
		}

		return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
	}

	// =========================
	// GETTERS / SETTERS
	// =========================

	public String getTrackingCode() {
		return trackingCode;
	}

	public void setTrackingCode(String trackingCode) {
		this.trackingCode = trackingCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderPhone() {
		return senderPhone;
	}

	public void setSenderPhone(String senderPhone) {
		this.senderPhone = senderPhone;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getReceiverPhone() {
		return receiverPhone;
	}

	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}

	public String getPickupProvince() {
		return pickupProvince;
	}

	public void setPickupProvince(String pickupProvince) {
		this.pickupProvince = pickupProvince;
	}

	public String getDeliveryDistrict() {
		return deliveryDistrict;
	}

	public void setDeliveryDistrict(String deliveryDistrict) {
		this.deliveryDistrict = deliveryDistrict;
	}

	public String getDeliveryProvince() {
		return deliveryProvince;
	}

	public void setDeliveryProvince(String deliveryProvince) {
		this.deliveryProvince = deliveryProvince;
	}

	public BigDecimal getWeightKg() {
		return weightKg;
	}

	public void setWeightKg(BigDecimal weightKg) {
		this.weightKg = weightKg;
	}

	public BigDecimal getCodAmount() {
		return codAmount;
	}

	public void setCodAmount(BigDecimal codAmount) {
		this.codAmount = codAmount;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	public String getShipperName() {
		return shipperName;
	}

	public void setShipperName(String shipperName) {
		this.shipperName = shipperName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public List<TrackingEvent> getEvents() {
		return events;
	}

	public void setEvents(List<TrackingEvent> events) {
		this.events = events;
	}
}