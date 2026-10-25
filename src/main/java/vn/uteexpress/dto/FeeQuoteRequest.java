package vn.uteexpress.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import vn.uteexpress.entity.ServiceType;

public class FeeQuoteRequest {

	@NotBlank(message = "Vui lòng nhập quận/huyện gửi hàng")
	@Size(max = 100)
	private String pickupDistrict;

	@NotBlank(message = "Vui lòng nhập quận/huyện nhận hàng")
	@Size(max = 100)
	private String deliveryDistrict;

	@NotNull(message = "Vui lòng nhập cân nặng")
	@DecimalMin(value = "0.01", message = "Cân nặng phải lớn hơn 0")
	@DecimalMax(value = "50.00", message = "Cân nặng tối đa 50 kg")
	@Digits(integer = 3, fraction = 2)
	private BigDecimal weightKg;

	@Positive(message = "Kích thước phải lớn hơn 0")
	private Integer lengthCm;

	@Positive(message = "Kích thước phải lớn hơn 0")
	private Integer widthCm;

	@Positive(message = "Kích thước phải lớn hơn 0")
	private Integer heightCm;

	@DecimalMin(value = "0", message = "Tiền thu hộ không được âm")
	@Digits(integer = 12, fraction = 2)
	private BigDecimal codAmount = BigDecimal.ZERO;

	@NotNull(message = "Vui lòng chọn dịch vụ")
	private ServiceType serviceType = ServiceType.STANDARD;

	@AssertTrue(message = "Vui lòng nhập đủ cả dài, rộng và cao")
	public boolean isDimensionsComplete() {
		return (lengthCm == null && widthCm == null && heightCm == null)
				|| (lengthCm != null && widthCm != null && heightCm != null);
	}

	public String getPickupDistrict() {
		return pickupDistrict;
	}

	public void setPickupDistrict(String pickupDistrict) {
		this.pickupDistrict = pickupDistrict;
	}

	public String getDeliveryDistrict() {
		return deliveryDistrict;
	}

	public void setDeliveryDistrict(String deliveryDistrict) {
		this.deliveryDistrict = deliveryDistrict;
	}

	public BigDecimal getWeightKg() {
		return weightKg;
	}

	public void setWeightKg(BigDecimal weightKg) {
		this.weightKg = weightKg;
	}

	public Integer getLengthCm() {
		return lengthCm;
	}

	public void setLengthCm(Integer lengthCm) {
		this.lengthCm = lengthCm;
	}

	public Integer getWidthCm() {
		return widthCm;
	}

	public void setWidthCm(Integer widthCm) {
		this.widthCm = widthCm;
	}

	public Integer getHeightCm() {
		return heightCm;
	}

	public void setHeightCm(Integer heightCm) {
		this.heightCm = heightCm;
	}

	public BigDecimal getCodAmount() {
		return codAmount;
	}

	public void setCodAmount(BigDecimal codAmount) {
		this.codAmount = codAmount;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}
}
