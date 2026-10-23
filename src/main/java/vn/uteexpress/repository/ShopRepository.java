package vn.uteexpress.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByVendorId(Long vendorId);

    boolean existsByVendorId(Long vendorId);

    List<Shop> findByNameContainingIgnoreCase(String keyword);

    List<Shop> findByActiveTrue();

    List<Shop> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);

    Optional<Shop> findByIdAndVendorId(Long shopId, Long vendorId);

    boolean existsByIdAndVendorId(Long shopId, Long vendorId);
}