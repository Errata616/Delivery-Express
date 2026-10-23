package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.uteexpress.entity.ShipmentEvent;
import vn.uteexpress.entity.ShipmentStatus;

public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, Long> {

	// Full timeline, newest first. Loads hubs in the same query to avoid N+1.
	@EntityGraph(attributePaths = { "hub" })
	List<ShipmentEvent> findByShipmentIdOrderByCreatedAtDescIdDesc(Long shipmentId);

	// Latest event of a shipment
	Optional<ShipmentEvent> findFirstByShipmentIdOrderByCreatedAtDescIdDesc(Long shipmentId);

	// E.g. count FAILED events to enforce "return to sender after 3 failed attempts"
	long countByShipmentIdAndStatus(Long shipmentId, ShipmentStatus status);

	@Modifying
	@Query("update ShipmentEvent e set e.updatedBy = null where e.updatedBy.id = :userId")
	int clearUpdatedBy(@Param("userId") Long userId);
}