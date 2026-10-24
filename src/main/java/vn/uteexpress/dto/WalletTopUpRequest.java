package vn.uteexpress.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WalletTopUpRequest(
		@NotNull(message = "Vui lòng nhập số tiền cần nạp")
		@Positive(message = "Số tiền nạp phải lớn hơn 0")
		@Digits(integer = 8, fraction = 0, message = "Số tiền nạp phải là số nguyên VND")
		@Max(value = 50_000_000, message = "Mỗi lần nạp tối đa 50.000.000 VND")
		BigDecimal amount) {
}
