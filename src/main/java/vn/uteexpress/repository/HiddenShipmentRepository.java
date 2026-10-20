package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.HiddenShipment;

public interface HiddenShipmentRepository extends JpaRepository<HiddenShipment, Long> {

	List<HiddenShipment> findByUserId(Long userId);

	Optional<HiddenShipment> findByUserIdAndShipmentId(Long userId, Long shipmentId);

	void deleteByShipmentId(Long shipmentId);
}
