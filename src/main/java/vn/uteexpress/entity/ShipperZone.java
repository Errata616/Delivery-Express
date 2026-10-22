package vn.uteexpress.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "shipper_zones", uniqueConstraints = @UniqueConstraint(columnNames = { "shipper_id", "district_code" }))
public class ShipperZone {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shipper_id", nullable = false)
	private User shipper;

	// Code from routing/districts.csv, e.g. "Q1", "BTH"
	@Column(name = "district_code", nullable = false, length = 10)
	private String districtCode;

	public ShipperZone() {
	}

	public ShipperZone(User shipper, String districtCode) {
		this.shipper = shipper;
		this.districtCode = districtCode;
	}

	public Long getId() {
		return id;
	}

	public User getShipper() {
		return shipper;
	}

	public String getDistrictCode() {
		return districtCode;
	}
}