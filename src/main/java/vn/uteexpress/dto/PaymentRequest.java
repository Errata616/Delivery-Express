package vn.uteexpress.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(@NotNull Long shipmentId) {
}
