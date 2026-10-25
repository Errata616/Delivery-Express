package vn.uteexpress.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hidden_shipments", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "shipment_id" }))
public class HiddenShipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shipment_id", nullable = false)
	private Shipment shipment;

	protected HiddenShipment() {
	}

	public HiddenShipment(User user, Shipment shipment) {
		this.user = user;
		this.shipment = shipment;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Shipment getShipment() {
		return shipment;
	}
}
