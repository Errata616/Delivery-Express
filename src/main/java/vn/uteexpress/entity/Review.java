package vn.uteexpress.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// One review per shipment
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shipment_id", nullable = false, unique = true)
	private Shipment shipment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private User customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shipper_id")
	private User shipper;

	@Column(nullable = false)
	private int rating;

	@Column(length = 1000)
	private String comment;

	public Review() {
	}

	public Review(Shipment shipment, User customer, User shipper, int rating, String comment) {
		this.shipment = shipment;
		this.customer = customer;
		this.shipper = shipper;
		this.rating = rating;
		this.comment = comment;
	}

	public Long getId() { return id; }
	public Shipment getShipment() { return shipment; }
	public User getCustomer() { return customer; }
	public User getShipper() { return shipper; }
	public int getRating() { return rating; }
	public String getComment() { return comment; }
}