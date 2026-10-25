package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByVendorId(Long vendorId);

    boolean existsByVendorId(Long vendorId);

    List<Delivery> findByNameContainingIgnoreCase(String keyword);

    List<Delivery> findByActiveTrue();

    List<Delivery> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);

    Optional<Delivery> findByIdAndVendorId(Long DeliveryId, Long vendorId);

    boolean existsByIdAndVendorId(Long DeliveryId, Long vendorId);
}