package vn.uteexpress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

	long countByStatus(OrderStatus status);

	long countByUserId(Long userId);

	@Query("select count(distinct s.order.id) from Shipment s "
			+ "where s.shipper.manager.id = :managerId and s.order.id is not null")
	long countByShipperManagerId(@Param("managerId") Long managerId);

	@Query("select count(distinct s.order.id) from Shipment s "
			+ "where s.shipper.manager.id = :managerId and s.order.status = :status")
	long countByShipperManagerIdAndStatus(@Param("managerId") Long managerId,
			@Param("status") OrderStatus status);
}