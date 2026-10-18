package vn.uteexpress.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.FeeZone;
import vn.uteexpress.entity.ShippingFeeRule;

public interface ShippingFeeRuleRepository extends JpaRepository<ShippingFeeRule, Long> {

	Optional<ShippingFeeRule> findFirstByZoneAndActiveTrueOrderByIdDesc(FeeZone zone);
}