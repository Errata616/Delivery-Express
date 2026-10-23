package vn.uteexpress.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 150)
	private String title;

	@Column(nullable = false, length = 500)
	private String body;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private NotificationType type = NotificationType.SYSTEM;

	// Optional link to the shipment this is about
	private Long shipmentId;

	@Column(nullable = false)
	private boolean seen = false;

	public Notification() {
	}

	public Notification(User user, String title, String body, NotificationType type, Long shipmentId) {
		this.user = user;
		this.title = title;
		this.body = body;
		this.type = type;
		this.shipmentId = shipmentId;
	}

	public Long getId() { return id; }
	public User getUser() { return user; }
	public String getTitle() { return title; }
	public String getBody() { return body; }
	public NotificationType getType() { return type; }
	public Long getShipmentId() { return shipmentId; }
	public boolean isSeen() { return seen; }
	public void setSeen(boolean seen) { this.seen = seen; }
}