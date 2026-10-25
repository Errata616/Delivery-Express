package vn.uteexpress.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.uteexpress.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsByShipmentId(Long shipmentId);

	Optional<Review> findByShipmentId(Long shipmentId);

	Page<Review> findByShipperIdOrderByCreatedAtDesc(Long shipperId, Pageable pageable);

	List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

	long countByShipperId(Long shipperId);

	long countByCustomerId(Long customerId);

	@Modifying
	@Query("update Review r set r.shipper = null where r.shipper.id = :shipperId")
	int clearShipper(@Param("shipperId") Long shipperId);

	@Query("select avg(r.rating) from Review r where r.shipper.id = :shipperId")
	Double averageRatingByShipper(@Param("shipperId") Long shipperId);
}