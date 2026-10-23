package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Address;
import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.AddressRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final AddressRepository addressRepository;
	private final UserRepository userRepository;
	private final ShipmentService shipmentService;

	public OrderService(OrderRepository orderRepository, AddressRepository addressRepository,
			UserRepository userRepository, ShipmentService shipmentService) {

		this.orderRepository = orderRepository;
		this.addressRepository = addressRepository;
		this.userRepository = userRepository;
		this.shipmentService = shipmentService;
	}

	// =========================================================
	// CREATE SHIPPING BOOKING
	// =========================================================

	@Transactional
	public Order createBooking(Long userId, Long addressId) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		if (addressId == null) {
			throw new IllegalArgumentException("Address ID không được null");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		Address address = addressRepository.findByIdAndUserId(addressId, userId)
				.orElseThrow(() -> new IllegalArgumentException("Địa chỉ giao hàng không thuộc người dùng"));

		Order order = new Order();
		order.setUser(user);
		order.setShippingAddress(address);
		order.setStatus(OrderStatus.PENDING);
		Order savedOrder = orderRepository.save(order);
		shipmentService.createShipmentForOrder(savedOrder.getId());
		return savedOrder;
	}

	// =========================================================
	// GET ORDERS BY USER
	// =========================================================

	@Transactional(readOnly = true)
	public List<Order> getOrdersByUser(Long userId) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	// =========================================================
	// GET ORDERS BY USER + STATUS
	// =========================================================

	@Transactional(readOnly = true)
	public List<Order> getOrdersByUserAndStatus(Long userId, OrderStatus status) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		if (status == null) {

			return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
		}

		return orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
	}

	// =========================================================
	// GET ORDER
	// =========================================================

	@Transactional(readOnly = true)
	public Order getOrder(Long orderId) {

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
	}

	// =========================================================
	// CANCEL ORDER
	// =========================================================

	@Transactional
	public Order cancelOrder(Long orderId, Long userId) {

		if (orderId == null) {
			throw new IllegalArgumentException("Order ID không được null");
		}

		if (userId == null) {
			throw new IllegalArgumentException("User ID không được null");
		}

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

		// =====================================================
		// OWNERSHIP
		// =====================================================

		if (order.getUser() == null || !order.getUser().getId().equals(userId)) {

			throw new IllegalArgumentException("Đơn hàng không thuộc người dùng này");
		}

		// =====================================================
		// CHECK STATUS
		// =====================================================

		if (order.getStatus() == OrderStatus.CANCELLED) {

			throw new IllegalArgumentException("Đơn hàng đã được hủy trước đó");
		}

		/*
		 * Chỉ cho phép hủy PENDING / CONFIRMED.
		 */
		if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {

			throw new IllegalArgumentException("Không thể hủy đơn hàng ở trạng thái " + order.getStatus());
		}

		shipmentService.cancelForOrder(orderId);
		order.setStatus(OrderStatus.CANCELLED);

		return orderRepository.save(order);
	}
}