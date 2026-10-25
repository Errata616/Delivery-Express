package vn.uteexpress.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "deliveries")
public class Delivery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(length = 1000)
	private String description;

	@Column(length = 255)
	private String address;

	@Column(length = 20)
	private String phone;

	@Column(length = 500)
	private String logo;

	@Column(nullable = false)
	private boolean active = true;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "vendor_id", nullable = false, unique = true)
	private User vendor;


	public Delivery() {
	}

	public Delivery(String name, String description, String address, String phone, String logo, boolean active,
			User vendor) {
		this.name = name;
		this.description = description;
		this.address = address;
		this.phone = phone;
		this.logo = logo;
		this.active = active;
		this.vendor = vendor;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public User getVendor() {
		return vendor;
	}

	public void setVendor(User vendor) {
		this.vendor = vendor;
	}
}