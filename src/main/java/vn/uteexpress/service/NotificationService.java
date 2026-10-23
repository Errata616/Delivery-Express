package vn.uteexpress.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import vn.uteexpress.dto.NotificationDto;
import vn.uteexpress.entity.Notification;
import vn.uteexpress.entity.NotificationType;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.NotificationRepository;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository notificationRepository;
	private final SimpMessagingTemplate messagingTemplate;

	public NotificationService(NotificationRepository notificationRepository,
			SimpMessagingTemplate messagingTemplate) {
		this.notificationRepository = notificationRepository;
		this.messagingTemplate = messagingTemplate;
	}

	// Save a notification and push it live to the user's "/user/queue/notifications"
	@Transactional
	public void notify(User user, String title, String body, NotificationType type, Long shipmentId) {

		if (user == null) {
			return;
		}

		Notification saved = notificationRepository.save(new Notification(user, title, body, type, shipmentId));

		pushAfterCommit(user.getUsername(), NotificationDto.from(saved));
	}

	@Transactional(readOnly = true)
	public Page<Notification> list(Long userId, Pageable pageable) {
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
	}

	@Transactional(readOnly = true)
	public long countUnseen(Long userId) {
		return notificationRepository.countByUserIdAndSeenFalse(userId);
	}

	@Transactional
	public Notification markSeen(Long notificationId, Long userId) {

		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

		// Same message as "not found", so nobody can probe other users' notification ids
		if (!notification.getUser().getId().equals(userId)) {
			throw new RuntimeException("Không tìm thấy thông báo");
		}

		notification.setSeen(true);

		return notificationRepository.save(notification);
	}

	@Transactional
	public int markAllSeen(Long userId) {
		return notificationRepository.markAllSeen(userId);
	}

	private void pushAfterCommit(String username, NotificationDto dto) {

		Runnable push = () -> {
			try {
				messagingTemplate.convertAndSendToUser(username, "/queue/notifications", dto);
			} catch (Exception ex) {
				log.warn("Could not push notification: {}", ex.getMessage());
			}
		};

		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					push.run();
				}
			});
		} else {
			push.run();
		}
	}
}