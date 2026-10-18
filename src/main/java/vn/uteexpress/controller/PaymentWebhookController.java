package vn.uteexpress.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.dto.PaymentResponse;
import vn.uteexpress.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

	private final PaymentService paymentService;

	public PaymentWebhookController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/webhook")
	public ResponseEntity<ApiResponse<PaymentResponse>> handleWebhook(
			@RequestHeader(value = "x-signature", required = false) String signature,
			@RequestBody String rawBody) {
		return ResponseEntity.ok(ApiResponse.ok(
				paymentService.handleWebhook(rawBody.getBytes(StandardCharsets.UTF_8), signature)));
	}
}
