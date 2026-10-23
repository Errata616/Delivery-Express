package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;
	private final UserRepository userRepository;

	public OrderController(OrderService orderService, UserRepository userRepository) {

		this.orderService = orderService;
		this.userRepository = userRepository;
	}

	// =========================================================
	// GET CURRENT USER
	// =========================================================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}

	// =========================================================
	// CHECK USER ACCESS
	// =========================================================

	private void checkUserAccess(Long userId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		if (!isAdmin && !currentUser.getId().equals(userId)) {

			throw new IllegalArgumentException("Không được truy cập dữ liệu " + "của người dùng khác");
		}
	}

	// =========================================================
	// CREATE SHIPPING BOOKING
	// =========================================================

	@PostMapping("/user/{userId}/booking")
	public ResponseEntity<Order> createBooking(@PathVariable Long userId, @RequestParam Long addressId,
			Authentication authentication) {

		checkUserAccess(userId, authentication);

		return ResponseEntity.ok(orderService.createBooking(userId, addressId));
	}

	// =========================================================
	// GET USER ORDERS
	// =========================================================

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Order>> getOrders(@PathVariable Long userId,
			@RequestParam(required = false) String status, Authentication authentication) {

		checkUserAccess(userId, authentication);

		OrderStatus orderStatus = null;

		if (status != null && !status.trim().isEmpty()) {

			try {
				orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());

			} catch (IllegalArgumentException e) {

				throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + status);
			}
		}

		return ResponseEntity.ok(orderService.getOrdersByUserAndStatus(userId, orderStatus));
	}

	// =========================================================
	// GET ORDER DETAIL
	// =========================================================

	@GetMapping("/{orderId}")
	public ResponseEntity<Order> getOrder(@PathVariable Long orderId, Authentication authentication) {

		Order order = orderService.getOrder(orderId);

		User currentUser = getCurrentUser(authentication);

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		if (!isAdmin && (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId()))) {

			throw new IllegalArgumentException("Không được xem đơn hàng " + "của người dùng khác");
		}

		return ResponseEntity.ok(order);
	}
	// =========================================================
	// CANCEL ORDER
	// =========================================================

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		Order cancelledOrder = orderService.cancelOrder(orderId, currentUser.getId());

		return ResponseEntity.ok(cancelledOrder);
	}
}