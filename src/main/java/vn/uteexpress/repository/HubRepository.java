package vn.uteexpress.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.uteexpress.entity.Hub;

public interface HubRepository extends JpaRepository<Hub, Long> {

	List<Hub> findByActiveTrue();

	List<Hub> findByProvinceIgnoreCaseAndActiveTrue(String province);

	boolean existsByCodeIgnoreCase(String code);

	boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}