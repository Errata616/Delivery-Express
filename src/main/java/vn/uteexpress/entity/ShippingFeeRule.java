package vn.uteexpress.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "shipping_fee_rules")
public class ShippingFeeRule extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FeeZone zone;

	// Price for the first 0.5 kg
	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal baseFee;

	// Added for every extra 0.5 kg
	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal stepFee;

	@Column(nullable = false)
	private boolean active = true;

	public ShippingFeeRule() {
	}

	public ShippingFeeRule(FeeZone zone, BigDecimal baseFee, BigDecimal stepFee) {
		this.zone = zone;
		this.baseFee = baseFee;
		this.stepFee = stepFee;
	}

	public Long getId() { return id; }
	public FeeZone getZone() { return zone; }
	public void setZone(FeeZone zone) { this.zone = zone; }
	public BigDecimal getBaseFee() { return baseFee; }
	public void setBaseFee(BigDecimal baseFee) { this.baseFee = baseFee; }
	public BigDecimal getStepFee() { return stepFee; }
	public void setStepFee(BigDecimal stepFee) { this.stepFee = stepFee; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
}