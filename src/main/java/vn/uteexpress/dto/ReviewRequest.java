package vn.uteexpress.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
		@NotNull(message = "Thiếu mã vận đơn") Long shipmentId,
		@NotNull(message = "Vui lòng chọn số sao") @Min(value = 1, message = "Tối thiểu 1 sao") @Max(value = 5, message = "Tối đa 5 sao") Integer rating,
		@Size(max = 1000, message = "Nhận xét tối đa 1000 ký tự") String comment) {
}