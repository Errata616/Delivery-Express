package vn.uteexpress.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import vn.uteexpress.entity.PaymentMethod;
import vn.uteexpress.entity.ServiceType;

public class BookingRequest {

    private static final String PHONE_REGEX = "^(\\+84|0)\\d{9,10}$";

    // =========================
    // SENDER (pickup)
    // =========================

    @NotBlank(message = "Tên người gửi không được để trống")
    @Size(max = 100)
    private String senderName;

    @NotBlank(message = "Số điện thoại người gửi không được để trống")
    @Pattern(regexp = PHONE_REGEX, message = "Số điện thoại người gửi không hợp lệ")
    private String senderPhone;

    @NotBlank(message = "Địa chỉ lấy hàng không được để trống")
    @Size(max = 255)
    private String pickupAddress;

    @Size(max = 100)
    private String pickupDistrict;

    @NotBlank(message = "Tỉnh/thành phố lấy hàng không được để trống")
    @Size(max = 100)
    private String pickupProvince;

    // =========================
    // RECEIVER (delivery)
    // =========================

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100)
    private String receiverName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Pattern(regexp = PHONE_REGEX, message = "Số điện thoại người nhận không hợp lệ")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 255)
    private String deliveryAddress;

    @Size(max = 100)
    private String deliveryDistrict;

    @NotBlank(message = "Tỉnh/thành phố giao hàng không được để trống")
    @Size(max = 100)
    private String deliveryProvince;

    // =========================
    // PACKAGE
    // =========================

    @NotBlank(message = "Mô tả hàng hóa không được để trống")
    @Size(max = 255)
    private String itemDescription;

    @NotNull(message = "Cân nặng không được để trống")
    @DecimalMin(value = "0.01", message = "Cân nặng phải lớn hơn 0")
    @DecimalMax(value = "50.00", message = "Cân nặng tối đa 50 kg")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal weightKg;

    // Optional, used later for volumetric weight
    @Positive
    private Integer lengthCm;

    @Positive
    private Integer widthCm;

    @Positive
    private Integer heightCm;

    // =========================
    // MONEY
    // =========================

    // Cash on delivery: amount the shipper collects from the receiver. 0 = no COD.
    @DecimalMin(value = "0", message = "Tiền thu hộ không được âm")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal codAmount = BigDecimal.ZERO;

    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @Size(max = 500)
    private String note;

    // =========================
    // GETTERS / SETTERS
    // =========================

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    // Strip spaces before validation, so "0901 234 567" is accepted
    public void setSenderPhone(String senderPhone) {
        this.senderPhone = normalizePhone(senderPhone);
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getPickupDistrict() {
        return pickupDistrict;
    }

    public void setPickupDistrict(String pickupDistrict) {
        this.pickupDistrict = pickupDistrict;
    }

    public String getPickupProvince() {
        return pickupProvince;
    }

    public void setPickupProvince(String pickupProvince) {
        this.pickupProvince = pickupProvince;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = normalizePhone(receiverPhone);
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // ---- add with the other fields ----
    @NotNull(message = "Vui lòng chọn dịch vụ")
    private ServiceType serviceType = ServiceType.STANDARD;

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    private static String normalizePhone(String phone) {

        return phone == null ? null : phone.trim().replaceAll("\\s+", "");
    }
}