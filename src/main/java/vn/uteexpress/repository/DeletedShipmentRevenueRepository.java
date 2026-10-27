package vn.uteexpress.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.uteexpress.entity.DeletedShipmentRevenue;

public interface DeletedShipmentRevenueRepository extends JpaRepository<DeletedShipmentRevenue, Long> {

	@Query("select sum(r.amount) from DeletedShipmentRevenue r")
	BigDecimal sumAmount();

	@Query("select sum(r.amount) from DeletedShipmentRevenue r where r.managerId = :managerId")
	BigDecimal sumAmountByManagerId(@Param("managerId") Long managerId);
}
