package vn.uteexpress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

	long countByStatus(OrderStatus status);

	long countByUserId(Long userId);
}