package vn.uteexpress.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.dto.BookingRequest;
import vn.uteexpress.dto.TrackingResponse;
import vn.uteexpress.entity.Address;
import vn.uteexpress.entity.Hub;
import vn.uteexpress.entity.HiddenShipment;
import vn.uteexpress.entity.NotificationType;
import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.Payment;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentEvent;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.HubRepository;
import vn.uteexpress.repository.HiddenShipmentRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.PaymentRepository;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.ShipmentEventRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class ShipmentService {

	private final ShipmentRepository shipmentRepository;
	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final HiddenShipmentRepository hiddenShipmentRepository;
	private final UserRepository userRepository;
	private final ShipmentEventRepository shipmentEventRepository;
	private final ReviewRepository reviewRepository;
	private final HubRepository hubRepository;
	private final ShippingFeeService shippingFeeService;
	private final NotificationService notificationService; // NEW

	public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository,
			PaymentRepository paymentRepository, HiddenShipmentRepository hiddenShipmentRepository,
			UserRepository userRepository, ShipmentEventRepository shipmentEventRepository,
			ReviewRepository reviewRepository, HubRepository hubRepository, ShippingFeeService shippingFeeService,
			NotificationService notificationService) { // NEW

		this.shipmentRepository = shipmentRepository;
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
		this.hiddenShipmentRepository = hiddenShipmentRepository;
		this.userRepository = userRepository;
		this.shipmentEventRepository = shipmentEventRepository;
		this.reviewRepository = reviewRepository;
		this.hubRepository = hubRepository;
		this.shippingFeeService = shippingFeeService;
		this.notificationService = notificationService; // NEW
	}

	// =========================================================
	// CREATE INDEPENDENT BOOKING (no shop order needed)
	// =========================================================

	@Transactional
	public Shipment createBooking(Long customerId, BookingRequest request) {

		if (customerId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		if (request == null) {
			throw new IllegalArgumentException("Thông tin đơn gửi hàng không được null");
		}

		User customer = userRepository.findById(customerId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		ShippingFeeService.FeeQuote fee = shippingFeeService.calculate(request);

		Shipment shipment = new Shipment();
		shipment.setTrackingCode(generateTrackingCode());
		shipment.setCustomer(customer);
		shipment.setStatus(ShipmentStatus.READY);

		shipment.setSenderName(request.getSenderName());
		shipment.setSenderPhone(request.getSenderPhone());
		shipment.setPickupAddress(request.getPickupAddress());
		shipment.setPickupDistrict(request.getPickupDistrict());
		shipment.setPickupProvince(request.getPickupProvince());

		shipment.setReceiverName(request.getReceiverName());
		shipment.setReceiverPhone(request.getReceiverPhone());
		shipment.setDeliveryAddress(request.getDeliveryAddress());
		shipment.setDeliveryDistrict(request.getDeliveryDistrict());
		shipment.setDeliveryProvince(request.getDeliveryProvince());

		shipment.setItemDescription(request.getItemDescription());
		shipment.setWeightKg(request.getWeightKg());
		shipment.setLengthCm(request.getLengthCm());
		shipment.setWidthCm(request.getWidthCm());
		shipment.setHeightCm(request.getHeightCm());
		shipment.setNote(request.getNote());

		shipment.setCodAmount(request.getCodAmount() == null ? BigDecimal.ZERO : request.getCodAmount());
		shipment.setServiceType(fee.serviceType());
		shipment.setShippingFee(fee.totalFee());

		shipment.setOriginHub(findHubByProvince(request.getPickupProvince()));
		shipment.setDestinationHub(findHubByProvince(request.getDeliveryProvince()));

		Shipment saved = shipmentRepository.save(shipment);

		recordEvent(saved, ShipmentStatus.READY, null, defaultNote(ShipmentStatus.READY), customer);

		notifyCustomer(saved, defaultNote(ShipmentStatus.READY)); // NEW

		return saved;
	}

	// =========================================================
	// FEE QUOTE (price preview, saves nothing)
	// =========================================================

	public ShippingFeeService.FeeQuote quote(BookingRequest request) {

		return shippingFeeService.calculate(request);
	}

	// =========================================================
	// ASSIGN SHIPPER
	// =========================================================

	@Transactional
	public Shipment assignShipper(Long shipmentId, Long shipperId) {

		if (shipmentId == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}

		if (shipperId == null) {
			throw new IllegalArgumentException("Shipper ID không được null");
		}

		Shipment shipment = findShipment(shipmentId);

		User shipper = findShipper(shipperId);

		if (shipment.getStatus() != ShipmentStatus.READY) {

			throw new IllegalArgumentException("Chỉ Shipment ở trạng thái READY mới được gán Shipper");
		}

		if (shipment.getShipper() != null) {

			throw new IllegalArgumentException("Shipment đã được gán Shipper");
		}

		if (shipment.getOrder() == null
				&& !paymentRepository.existsByShipmentIdAndStatus(shipmentId, "PAID")) {
			throw new IllegalArgumentException("Vui lòng thanh toán phí vận chuyển trước khi phân công");
		}

		shipment.setShipper(shipper);
		shipment.setStatus(ShipmentStatus.ASSIGNED);
		shipment.setAssignedAt(LocalDateTime.now());

		if (shipment.getOrder() != null) {
			shipment.getOrder().setStatus(OrderStatus.CONFIRMED);
		}

		Shipment saved = shipmentRepository.save(shipment);

		String assignNote = "Đã phân công cho shipper " + displayName(shipper);

		recordEvent(saved, ShipmentStatus.ASSIGNED, hubForStatus(saved, ShipmentStatus.ASSIGNED), assignNote,
				getActorOrNull());

		// NEW: tell the customer, and tell the shipper they have a new job
		notifyCustomer(saved, assignNote);

		notificationService.notify(shipper, "Đơn mới được giao cho bạn", "Vận đơn " + saved.getTrackingCode(),
				NotificationType.SHIPMENT_ASSIGNED, saved.getId());

		return saved;
	}

	// =========================================================
	// GET SHIPPERS
	// =========================================================

	@Transactional(readOnly = true)
	public List<User> getShippers() {

		return userRepository.findByRoleName("SHIPPER");
	}

	// =========================================================
	// UPDATE SHIPMENT STATUS
	// =========================================================

	@Transactional
	public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {

		return updateStatus(shipmentId, newStatus, null);
	}

	@Transactional
	public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus, String note) {

		if (shipmentId == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}

		if (newStatus == null) {
			throw new IllegalArgumentException("Trạng thái vận đơn không được null");
		}

		Shipment shipment = findShipment(shipmentId);

		ShipmentStatus currentStatus = shipment.getStatus();

		validateStatusTransition(currentStatus, newStatus);

		shipment.setStatus(newStatus);

		LocalDateTime now = LocalDateTime.now();

		if (newStatus == ShipmentStatus.PICKED_UP) {
			shipment.setPickedUpAt(now);
		}

		if (newStatus == ShipmentStatus.DELIVERED) {
			shipment.setDeliveredAt(now);
		}

		boolean paymentRefunded = false;
		if (newStatus == ShipmentStatus.FAILED) {
			Payment payment = paymentRepository
					.findFirstByShipmentIdAndStatusOrderByCreatedAtDesc(shipmentId, "PAID").orElse(null);
			if (payment != null) {
				payment.setStatus("REFUNDED");
				payment.setUpdatedAt(now);
				paymentRepository.save(payment);
				paymentRefunded = true;
			}
		}

		// Legacy shop orders are kept in sync; independent bookings have no order
		Order order = shipment.getOrder();

		if (order != null) {

			switch (newStatus) {
			case PICKED_UP:
			case DELIVERING:
				order.setStatus(OrderStatus.SHIPPING);
				break;
			case DELIVERED:
				order.setStatus(OrderStatus.DELIVERED);
				break;
			case CANCELLED:
				order.setStatus(OrderStatus.CANCELLED);
				break;
			case FAILED:
				order.setStatus(OrderStatus.FAILED);
				break;
			default:
				break;
			}

			orderRepository.save(order);
		}

		Shipment saved = shipmentRepository.save(shipment);

		String eventNote = (note == null || note.isBlank()) ? defaultNote(newStatus) : note.trim();
		if (paymentRefunded) {
			eventNote += ". Phí vận chuyển đã được hoàn lại (mô phỏng).";
		}

		recordEvent(saved, newStatus, hubForStatus(saved, newStatus), eventNote, getActorOrNull());

		notifyCustomer(saved, eventNote); // NEW

		return saved;
	}

	@Transactional
	public Shipment cancelForOrder(Long orderId) {
		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		Shipment shipment = shipmentRepository.findByOrderId(orderId).stream().findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Đơn gửi hàng chưa có vận đơn"));
		return updateStatus(shipment.getId(), ShipmentStatus.CANCELLED);
	}

	// =========================================================
	// GET SHIPMENT BY ID
	// =========================================================

	@Transactional(readOnly = true)
	public Shipment getById(Long id) {

		if (id == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}

		return findShipment(id);
	}

	// =========================================================
	// GET SHIPMENTS BY ORDER
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getByOrderId(Long orderId) {

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);

		if (shipments.isEmpty()) {

			throw new RuntimeException("Đơn hàng chưa có vận đơn");
		}

		return shipments;
	}

	// =========================================================
	// GET MY SHIPMENTS (customer)
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getMyShipments(Long userId) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		return filterHiddenShipments(userId, shipmentRepository.findAllByOwner(userId));
	}

	@Transactional
	public void hideShipmentFromView(Long shipmentId, User user) {
		if (shipmentId == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}
		if (user == null || user.getId() == null || user.getRole() == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		Shipment shipment = findShipment(shipmentId);
		Order order = shipment.getOrder();
		String role = user.getRole().getName();
		boolean ownershipMismatch = (shipment.getCustomer() != null
				&& !user.getId().equals(shipment.getCustomer().getId()))
				|| (order != null && order.getUser() != null && !user.getId().equals(order.getUser().getId()));
		boolean allowed = switch (role.toUpperCase()) {
		case "USER" -> (shipment.getCustomer() != null && user.getId().equals(shipment.getCustomer().getId()))
				|| (order != null && order.getUser() != null && user.getId().equals(order.getUser().getId()));
		case "SHIPPER" -> shipment.getShipper() != null && user.getId().equals(shipment.getShipper().getId());
		case "MANAGER" -> true;
		default -> false;
		};
		if ("USER".equalsIgnoreCase(role) && ownershipMismatch) {
			allowed = false;
		}
		if (!allowed) {
			throw new IllegalArgumentException("Không có quyền ẩn vận đơn này khỏi danh sách");
		}

		if (hiddenShipmentRepository.findByUserIdAndShipmentId(user.getId(), shipmentId).isEmpty()) {
			hiddenShipmentRepository.save(new HiddenShipment(user, shipment));
		}
	}

	@Transactional(readOnly = true)
	public List<Shipment> filterHiddenShipments(Long userId, List<Shipment> shipments) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}
		if (shipments.isEmpty()) {
			return shipments;
		}
		java.util.Set<Long> hiddenIds = hiddenShipmentRepository.findByUserId(userId).stream()
				.map(hidden -> hidden.getShipment().getId()).collect(java.util.stream.Collectors.toSet());
		return shipments.stream().filter(shipment -> !hiddenIds.contains(shipment.getId())).toList();
	}

	@Transactional
	public void deleteCompletedShipment(Long shipmentId) {
		if (shipmentId == null) {
			throw new IllegalArgumentException("Shipment ID không được null");
		}

		Shipment shipment = findShipment(shipmentId);
		Order order = shipment.getOrder();
		List<Payment> payments = new ArrayList<>(paymentRepository.findByShipmentId(shipmentId));
		if (order != null) {
			payments.addAll(paymentRepository.findByOrderId(order.getId()));
		}
		payments = new ArrayList<>(new LinkedHashSet<>(payments));
		boolean refunded = payments.stream()
				.anyMatch(payment -> "REFUNDED".equalsIgnoreCase(payment.getStatus()));
		if (shipment.getStatus() != ShipmentStatus.DELIVERED && !refunded) {
			throw new IllegalStateException("Chỉ có thể xóa vận đơn đã giao hoặc đã được hoàn tiền");
		}

		reviewRepository.findByShipmentId(shipmentId).ifPresent(reviewRepository::delete);
		shipmentEventRepository.deleteAll(
				shipmentEventRepository.findByShipmentIdOrderByCreatedAtDescIdDesc(shipmentId));
		paymentRepository.deleteAll(payments);
		hiddenShipmentRepository.deleteByShipmentId(shipmentId);
		shipmentRepository.delete(shipment);
		if (order != null) {
			orderRepository.delete(order);
		}
	}

	// =========================================================
	// GET BY TRACKING CODE (entity, internal use)
	// =========================================================

	@Transactional(readOnly = true)
	public Shipment getByTrackingCode(String code) {

		if (code == null || code.trim().isEmpty()) {

			throw new IllegalArgumentException("Tracking code không được để trống");
		}

		return shipmentRepository.findByTrackingCode(code.trim())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với mã: " + code));
	}

	// =========================================================
	// PUBLIC TRACKING (safe response with timeline)
	// =========================================================

	@Transactional(readOnly = true)
	public TrackingResponse getTracking(String code) {

		Shipment shipment = getByTrackingCode(code);

		String receiverName = shipment.getReceiverName();
		String receiverPhone = shipment.getReceiverPhone();
		String deliveryDistrict = shipment.getDeliveryDistrict();
		String deliveryProvince = shipment.getDeliveryProvince();

		// Legacy shipments created from a shop order: read the receiver from the order address
		Order order = shipment.getOrder();

		if (receiverName == null && order != null && order.getShippingAddress() != null) {

			Address address = order.getShippingAddress();

			receiverName = address.getReceiverName();
			receiverPhone = address.getPhone();
			deliveryDistrict = address.getDistrict();
			deliveryProvince = address.getCity();
		}

		TrackingResponse response = new TrackingResponse();

		response.setTrackingCode(shipment.getTrackingCode());
		response.setStatus(shipment.getStatus().name());
		response.setStatusText(statusText(shipment.getStatus()));

		response.setReceiverName(TrackingResponse.maskName(receiverName));
		response.setReceiverPhone(TrackingResponse.maskPhone(receiverPhone));

		response.setPickupProvince(shipment.getPickupProvince());
		response.setDeliveryDistrict(deliveryDistrict);
		response.setDeliveryProvince(deliveryProvince);

		response.setWeightKg(shipment.getWeightKg());
		response.setCodAmount(shipment.getCodAmount());

		if (shipment.getShipper() != null && shipment.getShipper().getFullName() != null
				&& !shipment.getShipper().getFullName().isBlank()) {
			response.setShipperName(shipment.getShipper().getFullName());
		}

		response.setCreatedAt(shipment.getCreatedAt());
		response.setDeliveredAt(shipment.getDeliveredAt());

		List<ShipmentEvent> events = shipmentEventRepository
				.findByShipmentIdOrderByCreatedAtDescIdDesc(shipment.getId());

		if (events.isEmpty()) {

			// Shipments created before the history table existed
			response.getEvents().add(new TrackingResponse.TrackingEvent(shipment.getStatus().name(),
					statusText(shipment.getStatus()), null, null, shipment.getCreatedAt()));

		} else {

			for (ShipmentEvent event : events) {

				String location = event.getHub() != null ? event.getHub().getName() : null;

				response.getEvents().add(new TrackingResponse.TrackingEvent(event.getStatus().name(),
						statusText(event.getStatus()), location, event.getNote(), event.getCreatedAt()));
			}
		}

		return response;
	}

	// =========================================================
	// GET SHIPMENTS BY SHIPPER
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getShipmentsByShipper(Long shipperId) {

		if (shipperId == null) {
			throw new IllegalArgumentException("Shipper ID không được null");
		}

		return shipmentRepository.findByShipperIdOrderByCreatedAtDesc(shipperId);
	}

	// =========================================================
	// GET UNASSIGNED SHIPMENTS
	// =========================================================

	@Transactional(readOnly = true)
	public List<Shipment> getUnassignedShipments() {

		return shipmentRepository.findByShipperIsNullOrderByCreatedAtDesc();
	}

	@Transactional(readOnly = true)
	public List<Shipment> getDeletableShipments() {
		Set<Long> refundedShipmentIds = new HashSet<>();
		Set<Long> refundedOrderIds = new HashSet<>();
		for (Payment payment : paymentRepository.findByStatusIgnoreCase("REFUNDED")) {
			if (payment.getShipment() != null) {
				refundedShipmentIds.add(payment.getShipment().getId());
			}
			if (payment.getOrder() != null) {
				refundedOrderIds.add(payment.getOrder().getId());
			}
		}
		return shipmentRepository.findAll().stream()
				.filter(shipment -> shipment.getStatus() == ShipmentStatus.DELIVERED
						|| refundedShipmentIds.contains(shipment.getId())
						|| (shipment.getOrder() != null && refundedOrderIds.contains(shipment.getOrder().getId())))
				.toList();
	}

	// =========================================================
	// FIND SHIPMENT
	// =========================================================

	private Shipment findShipment(Long id) {

		return shipmentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn với ID: " + id));
	}

	// =========================================================
	// FIND SHIPPER
	// =========================================================

	private User findShipper(Long id) {

		User shipper = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Shipper"));

		if (shipper.getRole() == null || !"SHIPPER".equalsIgnoreCase(shipper.getRole().getName())) {

			throw new IllegalArgumentException("User được chọn không có role SHIPPER");
		}

		if (!shipper.isEnabled()) {

			throw new IllegalArgumentException("Tài khoản Shipper đang bị khóa");
		}

		return shipper;
	}

	// =========================================================
	// GENERATE TRACKING CODE
	// =========================================================

	private String generateTrackingCode() {

		String code;

		do {

			code = "UTE" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

		} while (shipmentRepository.findByTrackingCode(code).isPresent());

		return code;
	}

	// =========================================================
	// EVENT HISTORY HELPERS
	// =========================================================

	private void recordEvent(Shipment shipment, ShipmentStatus status, Hub hub, String note, User actor) {

		String text = note;

		if (text != null && text.length() > 500) {
			text = text.substring(0, 500);
		}

		shipmentEventRepository.save(new ShipmentEvent(shipment, status, hub, text, actor));
	}

	// NEW: notify whoever owns the shipment (direct booking or legacy shop order)
	private void notifyCustomer(Shipment shipment, String body) {

		User customer = shipment.getCustomer();

		if (customer == null && shipment.getOrder() != null) {
			customer = shipment.getOrder().getUser();
		}

		String text = body;

		if (text != null && text.length() > 500) {
			text = text.substring(0, 500);
		}

		notificationService.notify(customer, "Vận đơn " + shipment.getTrackingCode(), text,
				NotificationType.SHIPMENT_STATUS, shipment.getId());
	}

	// The logged-in user who triggered the change, or null if there is none
	private User getActorOrNull() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}

		return userRepository.findByUsername(authentication.getName()).orElse(null);
	}

	// Pickup-side steps happen at the origin hub, delivery-side steps at the destination hub
	private Hub hubForStatus(Shipment shipment, ShipmentStatus status) {

		return switch (status) {
		case ASSIGNED, PICKED_UP -> shipment.getOriginHub();
		case DELIVERING, DELIVERED, FAILED -> shipment.getDestinationHub();
		default -> null;
		};
	}

	// Best-effort: first active hub whose province matches (ignores case, accents, "TP."/"Tỉnh")
	private Hub findHubByProvince(String province) {

		String wanted = shippingFeeService.normalizeProvince(province);

		if (wanted.isEmpty()) {
			return null;
		}

		return hubRepository.findByActiveTrue().stream()
				.filter(hub -> wanted.equals(shippingFeeService.normalizeProvince(hub.getProvince()))).findFirst()
				.orElse(null);
	}

	// Public-safe name: full name only, never the username
	private String displayName(User user) {

		if (user.getFullName() != null && !user.getFullName().isBlank()) {
			return user.getFullName();
		}

		return "của bưu cục";
	}

	private String defaultNote(ShipmentStatus status) {

		return switch (status) {
		case READY -> "Đơn hàng đã được tạo";
		case ASSIGNED -> "Đã phân công shipper";
		case PICKED_UP -> "Shipper đã lấy hàng";
		case DELIVERING -> "Đang giao hàng đến người nhận";
		case DELIVERED -> "Giao hàng thành công";
		case FAILED -> "Giao hàng thất bại";
		case CANCELLED -> "Đơn hàng đã bị hủy";
		default -> status.name();
		};
	}

	private String statusText(ShipmentStatus status) {

		return switch (status) {
		case READY -> "Đã tạo đơn";
		case ASSIGNED -> "Đã phân công shipper";
		case PICKED_UP -> "Đã lấy hàng";
		case DELIVERING -> "Đang giao hàng";
		case DELIVERED -> "Đã giao thành công";
		case FAILED -> "Giao hàng thất bại";
		case CANCELLED -> "Đã hủy";
		default -> status.name();
		};
	}

	// =========================================================
	// VALIDATE STATUS TRANSITION
	// =========================================================

	private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {

		if (current == null) {

			throw new IllegalArgumentException("Shipment hiện tại chưa có trạng thái");
		}

		if (next == null) {

			throw new IllegalArgumentException("Trạng thái mới không được null");
		}

		/*
		 * DELIVERED, CANCELLED và FAILED đều là trạng thái kết thúc.
		 */
		if (current == ShipmentStatus.DELIVERED || current == ShipmentStatus.CANCELLED
				|| current == ShipmentStatus.FAILED) {

			throw new IllegalArgumentException("Vận đơn đã kết thúc, không thể thay đổi trạng thái");
		}

		if (next == current) {

			throw new IllegalArgumentException("Trạng thái mới giống trạng thái hiện tại");
		}

		/*
		 * READY -> ASSIGNED
		 */
		if (next == ShipmentStatus.ASSIGNED && current != ShipmentStatus.READY) {

			throw new IllegalArgumentException("Chỉ READY mới chuyển sang ASSIGNED");
		}

		/*
		 * ASSIGNED -> PICKED_UP
		 */
		if (next == ShipmentStatus.PICKED_UP && current != ShipmentStatus.ASSIGNED) {

			throw new IllegalArgumentException("Chỉ ASSIGNED mới chuyển sang PICKED_UP");
		}

		/*
		 * PICKED_UP -> DELIVERING
		 */
		if (next == ShipmentStatus.DELIVERING && current != ShipmentStatus.PICKED_UP) {

			throw new IllegalArgumentException("Chỉ PICKED_UP mới chuyển sang DELIVERING");
		}

		/*
		 * DELIVERING -> DELIVERED
		 */
		if (next == ShipmentStatus.DELIVERED && current != ShipmentStatus.DELIVERING) {

			throw new IllegalArgumentException("Chỉ DELIVERING mới chuyển sang DELIVERED");
		}

		/*
		 * ASSIGNED / PICKED_UP / DELIVERING có thể chuyển FAILED.
		 */
		if (next == ShipmentStatus.FAILED && current != ShipmentStatus.ASSIGNED && current != ShipmentStatus.PICKED_UP
				&& current != ShipmentStatus.DELIVERING) {

			throw new IllegalArgumentException("Không thể đánh dấu FAILED ở trạng thái hiện tại");
		}

		/*
		 * Cho phép hủy Shipment trước khi hoàn tất.
		 */
		if (next == ShipmentStatus.CANCELLED) {

			if (current != ShipmentStatus.READY && current != ShipmentStatus.ASSIGNED
					&& current != ShipmentStatus.PICKED_UP && current != ShipmentStatus.DELIVERING) {

				throw new IllegalArgumentException("Không thể hủy Shipment ở trạng thái hiện tại");
			}
		}
	}

	// =========================================================
	// CREATE SHIPMENT FOR BOOKING (legacy order-based flow)
	// =========================================================

	@Transactional
	public Shipment createShipmentForOrder(Long orderId) {

		if (orderId == null) {

			throw new IllegalArgumentException("Order ID không được null");
		}

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn gửi hàng"));

		if (shipmentRepository.existsByOrderId(orderId)) {
			throw new IllegalArgumentException("Đơn gửi hàng đã có vận đơn");
		}

		Shipment shipment = new Shipment();
		shipment.setOrder(order);
		shipment.setCustomer(order.getUser());
		shipment.setStatus(ShipmentStatus.READY);
		shipment.setTrackingCode(generateTrackingCode());

		Shipment saved = shipmentRepository.save(shipment);

		recordEvent(saved, ShipmentStatus.READY, null, defaultNote(ShipmentStatus.READY), order.getUser());

		return saved;
	}
}