package vn.uteexpress.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import vn.uteexpress.entity.Payment;

public record PaymentResponse(Long id, Long shipmentId, Long orderId, BigDecimal amount, String currency, String status,
		String provider, String transactionCode, LocalDateTime createdAt, LocalDateTime updatedAt) {

	public static PaymentResponse from(Payment p) {
		return new PaymentResponse(p.getId(), p.getShipment() != null ? p.getShipment().getId() : null,
				p.getOrder() != null ? p.getOrder().getId() : null, p.getAmount(), p.getCurrency(), p.getStatus(),
				p.getProvider(), p.getTransactionCode(), p.getCreatedAt(),
				p.getUpdatedAt());
	}
}