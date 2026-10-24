package vn.uteexpress.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "shipments")
public class Shipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 30)
	private String trackingCode;

	// Legacy link to a shop order. Null for independent bookings.
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "order_id", unique = true)
	private Order order;

	// Who created the booking
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "customer_id")
	private User customer;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "shipper_id")
	private User shipper;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "origin_hub_id")
	private Hub originHub;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "destination_hub_id")
	private Hub destinationHub;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ShipmentStatus status = ShipmentStatus.READY;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ServiceType serviceType = ServiceType.STANDARD;

	// ---- sender (pickup) ----

	@Column(length = 100)
	private String senderName;

	@Column(length = 20)
	private String senderPhone;

	@Column(length = 255)
	private String pickupAddress;

	@Column(length = 100)
	private String pickupDistrict;

	@Column(length = 100)
	private String pickupProvince;

	// ---- receiver (delivery) ----

	@Column(length = 100)
	private String receiverName;

	@Column(length = 20)
	private String receiverPhone;

	@Column(length = 255)
	private String deliveryAddress;

	@Column(length = 100)
	private String deliveryDistrict;

	@Column(length = 100)
	private String deliveryProvince;

	@Column(name = "planned_route", length = 1000)
	private String plannedRoute;

	@Column(name = "current_route_index")
	private Integer currentRouteIndex;

	// ---- package ----

	@Column(length = 255)
	private String itemDescription;

	@Column(precision = 8, scale = 2)
	private BigDecimal weightKg;

	private Integer lengthCm;
	private Integer widthCm;
	private Integer heightCm;

	// ---- money ----

	@Column(precision = 15, scale = 2)
	private BigDecimal codAmount = BigDecimal.ZERO;

	@Column(precision = 15, scale = 2)
	private BigDecimal shippingFee = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", length = 20)
	private PaymentMethod paymentMethod;

	@Column(name = "payment_status", length = 20)
	private String paymentStatus;

	// ---- timestamps ----

	@Column(nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	private LocalDateTime assignedAt;
	private LocalDateTime pickedUpAt;
	private LocalDateTime deliveredAt;

	@Column(length = 500)
	private String note;

	public Shipment() {
	}

	public Long getId() {
		return id;
	}

	public String getTrackingCode() {
		return trackingCode;
	}

	public void setTrackingCode(String trackingCode) {
		this.trackingCode = trackingCode;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public User getCustomer() {
		return customer;
	}

	public void setCustomer(User customer) {
		this.customer = customer;
	}

	public User getShipper() {
		return shipper;
	}

	public void setShipper(User shipper) {
		this.shipper = shipper;
	}

	public Hub getOriginHub() {
		return originHub;
	}

	public void setOriginHub(Hub originHub) {
		this.originHub = originHub;
	}

	public Hub getDestinationHub() {
		return destinationHub;
	}

	public void setDestinationHub(Hub destinationHub) {
		this.destinationHub = destinationHub;
	}

	public ShipmentStatus getStatus() {
		return status;
	}

	public void setStatus(ShipmentStatus status) {
		this.status = status;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
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
		this.receiverPhone = receiverPhone;
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

	public List<String> getPlannedRouteDistricts() {
		return plannedRoute == null || plannedRoute.isBlank() ? List.of() : List.of(plannedRoute.split(","));
	}

	public void setPlannedRouteDistricts(List<String> districtCodes) {
		plannedRoute = districtCodes == null || districtCodes.isEmpty() ? null : String.join(",", districtCodes);
		currentRouteIndex = districtCodes == null || districtCodes.isEmpty() ? null : 0;
	}

	public Integer getCurrentRouteIndex() {
		return currentRouteIndex;
	}

	public void setCurrentRouteIndex(Integer currentRouteIndex) {
		this.currentRouteIndex = currentRouteIndex;
	}

	public String getCurrentDistrict() {
		List<String> route = getPlannedRouteDistricts();
		return currentRouteIndex == null || currentRouteIndex < 0 || currentRouteIndex >= route.size()
				? null
				: route.get(currentRouteIndex);
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

	public BigDecimal getShippingFee() {
		return shippingFee;
	}

	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = shippingFee;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(LocalDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}

	public LocalDateTime getPickedUpAt() {
		return pickedUpAt;
	}

	public void setPickedUpAt(LocalDateTime pickedUpAt) {
		this.pickedUpAt = pickedUpAt;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}