package vn.uteexpress.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.uteexpress.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	long countByUserIdAndSeenFalse(Long userId);

	@Modifying
	@Query("update Notification n set n.seen = true where n.user.id = :userId and n.seen = false")
	int markAllSeen(@Param("userId") Long userId);

	@Modifying
	@Query("delete from Notification n where n.user.id = :userId")
	int deleteAllByUserId(@Param("userId") Long userId);
}