package vn.uteexpress.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import vn.uteexpress.entity.Order;

public record OrderResponse(Long id, Long userId, AddressResponse shippingAddress, BigDecimal totalAmount,
		String status, LocalDateTime createdAt) {

	public static OrderResponse from(Order o) {
		return new OrderResponse(o.getId(), o.getUser() != null ? o.getUser().getId() : null,
				o.getShippingAddress() != null ? AddressResponse.from(o.getShippingAddress()) : null,
				o.getTotalAmount(), o.getStatus() != null ? o.getStatus().name() : null, o.getCreatedAt());
	}
}