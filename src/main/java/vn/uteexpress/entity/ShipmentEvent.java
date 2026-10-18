package vn.uteexpress.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/**
 * One row of a shipment's tracking history. Events are only ever added, never
 * edited, so there are getters but no setters.
 *
 * Relations are LAZY: always read them inside a @Transactional service method
 * (like the tracking mapping will) and never return this entity directly as JSON.
 */
@Entity
@Table(name = "shipment_events")
public class ShipmentEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shipment_id", nullable = false)
	private Shipment shipment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ShipmentStatus status;

	// Where it happened. Null when it is not tied to a hub.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hub_id")
	private Hub hub;

	@Column(length = 500)
	private String note;

	// Who triggered the change. Null for automatic/system events.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by_id")
	private User updatedBy;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	public ShipmentEvent() {
	}

	public ShipmentEvent(Shipment shipment, ShipmentStatus status, Hub hub, String note, User updatedBy) {
		this.shipment = shipment;
		this.status = status;
		this.hub = hub;
		this.note = note;
		this.updatedBy = updatedBy;
	}

	public Long getId() {
		return id;
	}

	public Shipment getShipment() {
		return shipment;
	}

	public ShipmentStatus getStatus() {
		return status;
	}

	public Hub getHub() {
		return hub;
	}

	public String getNote() {
		return note;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}