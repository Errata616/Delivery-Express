package vn.uteexpress.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hubs")
public class Hub extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Short unique code, e.g. "HCM-01"
	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 255)
	private String address;

	@Column(length = 100)
	private String district;

	@Column(nullable = false, length = 100)
	private String province;

	@Column(length = 20)
	private String phone;

	@Column(nullable = false)
	private boolean active = true;

	public Hub() {
	}

	public Hub(String code, String name, String address, String district, String province, String phone) {
		this.code = code;
		this.name = name;
		this.address = address;
		this.district = district;
		this.province = province;
		this.phone = phone;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}