package vn.uteexpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.uteexpress.entity.Payment;

import java.util.Optional;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findFirstByShipmentIdAndStatusOrderByCreatedAtDesc(Long shipmentId, String status);

	boolean existsByShipmentIdAndStatus(Long shipmentId, String status);

	List<Payment> findByShipmentId(Long shipmentId);

	List<Payment> findByOrderId(Long orderId);

	List<Payment> findByStatusIgnoreCase(String status);

	List<Payment> findByShipmentCustomerIdOrderByCreatedAtDesc(Long customerId);
}
