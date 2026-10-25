package vn.uteexpress.controller;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationWebSocketController {

	private final SimpMessagingTemplate messagingTemplate;

	public NotificationWebSocketController(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@MessageMapping("/notifications")
	@SendTo("/topic/notifications")
	public Map<String, Object> sendBroadcast(Map<String, Object> payload) {
		return payload;
	}

	@MessageMapping("/notifications/private")
	public void sendPrivate(Map<String, Object> payload) {
		String username = String.valueOf(payload.getOrDefault("to", ""));
		messagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload);
	}
}
