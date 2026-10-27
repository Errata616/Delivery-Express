package vn.uteexpress.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "deleted_shipment_revenue")
public class DeletedShipmentRevenue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	@Column(name = "manager_id")
	private Long managerId;

	@Column(nullable = false)
	private LocalDateTime recordedAt = LocalDateTime.now();

	protected DeletedShipmentRevenue() {
	}

	public DeletedShipmentRevenue(BigDecimal amount, Long managerId) {
		this.amount = amount;
		this.managerId = managerId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Long getManagerId() {
		return managerId;
	}
}
