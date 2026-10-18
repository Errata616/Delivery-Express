package vn.uteexpress.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.dto.NotificationDto;
import vn.uteexpress.dto.PageResponse;
import vn.uteexpress.entity.Notification;
import vn.uteexpress.security.CurrentUser;
import vn.uteexpress.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;
	private final CurrentUser currentUser;

	public NotificationController(NotificationService notificationService, CurrentUser currentUser) {
		this.notificationService = notificationService;
		this.currentUser = currentUser;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<NotificationDto>>> list(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

		Page<Notification> result = notificationService.list(currentUser.getUser().getId(),
				PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));

		return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, NotificationDto::from)));
	}

	@GetMapping("/unread-count")
	public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount() {

		long count = notificationService.countUnseen(currentUser.getUser().getId());

		return ResponseEntity.ok(ApiResponse.ok(Map.of("unread", count)));
	}

	@PutMapping("/{id}/read")
	public ResponseEntity<ApiResponse<NotificationDto>> markRead(@PathVariable Long id) {

		Notification notification = notificationService.markSeen(id, currentUser.getUser().getId());

		return ResponseEntity.ok(ApiResponse.ok(NotificationDto.from(notification)));
	}

	@PutMapping("/read-all")
	public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead() {

		int updated = notificationService.markAllSeen(currentUser.getUser().getId());

		return ResponseEntity.ok(ApiResponse.ok(Map.of("updated", updated)));
	}
}