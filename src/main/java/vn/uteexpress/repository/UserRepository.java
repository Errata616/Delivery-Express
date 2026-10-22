package vn.uteexpress.repository;

import vn.uteexpress.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	@EntityGraph(attributePaths = "manager")
	List<User> findByRoleName(String roleName);

	@EntityGraph(attributePaths = "manager")
	List<User> findByManager_Id(Long managerId);

	long countByRoleName(String roleName);

}