package vn.uteexpress.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.ShipperZone;

public interface ShipperZoneRepository extends JpaRepository<ShipperZone, Long> {

	List<ShipperZone> findByShipperId(Long shipperId);

	List<ShipperZone> findByShipperIdIn(Collection<Long> shipperIds);

	void deleteByShipperId(Long shipperId);
}