package vn.uteexpress.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

	List<Shipment> findByOrderId(Long orderId);

	Optional<Shipment> findByTrackingCode(String trackingCode);

	List<Shipment> findByShipperIdOrderByCreatedAtDesc(Long shipperId);

	List<Shipment> findByShipperIsNullOrderByCreatedAtDesc();

	boolean existsByOrderId(Long orderId);

	long countByCustomerId(Long customerId);

	long countByShipperIdAndStatusIn(Long shipperId, List<ShipmentStatus> statuses);

	@Modifying
	@Query("update Shipment s set s.shipper = null where s.shipper.id = :shipperId")
	int unassignShipper(@Param("shipperId") Long shipperId);

	List<Shipment> findByShipperIdAndStatusOrderByCreatedAtDesc(Long shipperId, ShipmentStatus status);

	List<Shipment> findByShipperIdAndStatusInOrderByCreatedAtDesc(Long shipperId, List<ShipmentStatus> statuses);

	long countByStatus(ShipmentStatus status);

	long countByShipper_Manager_Id(Long managerId);

	long countByShipper_Manager_IdAndStatus(Long managerId, ShipmentStatus status);

	long countByShipper_Manager_IdAndStatusAndDeliveredAtGreaterThanEqualAndDeliveredAtLessThan(Long managerId,
			ShipmentStatus status, LocalDateTime startInclusive, LocalDateTime endExclusive);

	long countByStatusAndDeliveredAtGreaterThanEqualAndDeliveredAtLessThan(ShipmentStatus status,
			LocalDateTime startInclusive, LocalDateTime endExclusive);

	@Query("select sum(s.shippingFee) from Shipment s where s.status = :status")
	BigDecimal sumShippingFeeByStatus(@Param("status") ShipmentStatus status);

	@Query("select sum(s.shippingFee) from Shipment s "
			+ "where s.status = :status and s.shipper.manager.id = :managerId")
	BigDecimal sumShippingFeeByShipperManagerIdAndStatus(@Param("managerId") Long managerId,
			@Param("status") ShipmentStatus status);

	@Query("select sum(s.codAmount) from Shipment s where s.status in :statuses")
	BigDecimal sumCodAmountByStatusIn(@Param("statuses") List<ShipmentStatus> statuses);

	@Query("select sum(s.codAmount) from Shipment s "
			+ "where s.status in :statuses and s.shipper.manager.id = :managerId")
	BigDecimal sumCodAmountByShipperManagerIdAndStatusIn(@Param("managerId") Long managerId,
			@Param("statuses") List<ShipmentStatus> statuses);
    
	// Shipments a user created directly, plus legacy ones that came through their orders
	@Query("select s from Shipment s left join s.order o "
			+ "where s.customer.id = :userId or o.user.id = :userId order by s.createdAt desc")
	List<Shipment> findAllByOwner(@Param("userId") Long userId);
}