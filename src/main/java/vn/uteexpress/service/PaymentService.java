package vn.uteexpress.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import vn.uteexpress.config.PaymentConfig;
import vn.uteexpress.dto.PaymentRequest;
import vn.uteexpress.dto.PaymentResponse;
import vn.uteexpress.entity.Payment;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.PaymentRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.security.CurrentUser;

@Service
public class PaymentService {

	private static final String SIMULATED_PROVIDER = "SIMULATED";
	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final PaymentRepository paymentRepository;
	private final ShipmentRepository shipmentRepository;
	private final CurrentUser currentUser;
	private final PaymentConfig paymentConfig;
	private final ObjectMapper objectMapper;

	public PaymentService(PaymentRepository paymentRepository, ShipmentRepository shipmentRepository,
			CurrentUser currentUser, PaymentConfig paymentConfig, ObjectMapper objectMapper) {
		this.paymentRepository = paymentRepository;
		this.shipmentRepository = shipmentRepository;
		this.currentUser = currentUser;
		this.paymentConfig = paymentConfig;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public PaymentResponse createPayment(PaymentRequest request) {
		if (request == null || request.shipmentId() == null) {
			throw new IllegalArgumentException("Shipment id is required");
		}

		User actor = currentUser.getUser();
		Shipment shipment = shipmentRepository.findById(request.shipmentId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
		requireOwnerOrAdmin(actor, shipment);

		if (shipment.getStatus() != ShipmentStatus.READY) {
			throw new IllegalArgumentException("Payment can only be created for a READY shipment");
		}
		if (shipment.getShippingFee() == null || shipment.getShippingFee().signum() <= 0) {
			throw new IllegalArgumentException("Shipment has no payable shipping fee");
		}

		// NEW: block paying twice (a shipment stays READY after it is paid)
		if (paymentRepository.existsByShipmentIdAndStatus(shipment.getId(), "PAID")) {
			throw new IllegalArgumentException("Shipment has already been paid");
		}

		Payment existingPayment = paymentRepository
				.findFirstByShipmentIdAndStatusOrderByCreatedAtDesc(shipment.getId(), "PENDING").orElse(null);
		if (existingPayment != null) {
			return PaymentResponse.from(existingPayment);
		}

		Payment payment = new Payment();
		payment.setShipment(shipment);
		payment.setAmount(shipment.getShippingFee());
		payment.setCurrency("VND");
		payment.setProvider(SIMULATED_PROVIDER);
		payment.setStatus("PENDING");
		payment.setCreatedAt(LocalDateTime.now());

		return PaymentResponse.from(paymentRepository.save(payment));
	}

	@Transactional(readOnly = true)
	public PaymentResponse getPaymentById(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Payment id is required");
		}

		Payment payment = findPayment(id);
		requireOwnerOrAdmin(currentUser.getUser(), payment.getShipment());
		return PaymentResponse.from(payment);
	}

	@Transactional
	public PaymentResponse cancelPayment(Long id) {
		Payment payment = findPayment(id);
		requireOwnerOrAdmin(currentUser.getUser(), payment.getShipment());
		if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
			throw new IllegalStateException("Only a pending payment can be cancelled");
		}

		payment.setStatus("CANCELLED");
		payment.setUpdatedAt(LocalDateTime.now());
		return PaymentResponse.from(paymentRepository.save(payment));
	}

	@Transactional(readOnly = true)
	public List<PaymentResponse> getMyPayments() {
		User actor = currentUser.getUser();
		return paymentRepository.findByShipmentCustomerIdOrderByCreatedAtDesc(actor.getId()).stream()
				.map(PaymentResponse::from).toList();
	}

	// DEV ONLY: stands in for the gateway callback, which the browser cannot sign.
	@Transactional
	public PaymentResponse simulatePayment(Long id) {
		if (!paymentConfig.isSimulationEnabled()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment simulation is disabled");
		}

		Payment payment = findPayment(id);
		requireOwnerOrAdmin(currentUser.getUser(), payment.getShipment());

		if (!SIMULATED_PROVIDER.equals(payment.getProvider())) {
			throw new IllegalArgumentException("Only simulated payments can be simulated");
		}
		if ("PAID".equalsIgnoreCase(payment.getStatus())) {
			return PaymentResponse.from(payment);
		}
		if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
			throw new IllegalArgumentException("Only a pending payment can be paid");
		}

		payment.setStatus("PAID");
		payment.setTransactionCode("SIM-" + payment.getId() + "-" + System.currentTimeMillis());
		payment.setUpdatedAt(LocalDateTime.now());
		return PaymentResponse.from(paymentRepository.save(payment));
	}

	@Transactional
	public PaymentResponse handleWebhook(byte[] rawBody, String signature) {
		verifySignature(rawBody, signature);

		WebhookPayload payload;
		try {
			payload = objectMapper.readValue(rawBody, WebhookPayload.class);
		} catch (JacksonException exception) {
			throw new IllegalArgumentException("Invalid webhook payload");
		}

		if (payload.paymentId() == null || payload.status() == null || payload.status().isBlank()) {
			throw new IllegalArgumentException("Invalid webhook payload");
		}

		Payment payment = findPayment(payload.paymentId());
		if (!SIMULATED_PROVIDER.equals(payment.getProvider())) {
			throw new IllegalArgumentException("Webhook provider does not match payment");
		}

		String status = payload.status().trim().toUpperCase(Locale.ROOT);
		if ("PAID".equals(status) || "SUCCESS".equals(status) || "COMPLETED".equals(status)) {
			if ("PAID".equalsIgnoreCase(payment.getStatus())) {
				return PaymentResponse.from(payment);
			}
			if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
				throw new IllegalArgumentException("Only a pending payment can be marked as paid");
			}
			payment.setStatus("PAID");
			payment.setTransactionCode(payload.transactionCode());
			payment.setUpdatedAt(LocalDateTime.now());
			return PaymentResponse.from(paymentRepository.save(payment));
		}

		if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
			if ("PAID".equalsIgnoreCase(payment.getStatus())) {
				throw new IllegalArgumentException("A paid payment cannot be changed");
			}
			if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
				return PaymentResponse.from(payment);
			}
			payment.setStatus(status);
			payment.setUpdatedAt(LocalDateTime.now());
			return PaymentResponse.from(paymentRepository.save(payment));
		}

		throw new IllegalArgumentException("Unsupported payment status");
	}

	private Payment findPayment(Long id) {
		return paymentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
	}

	private void requireOwnerOrAdmin(User actor, Shipment shipment) {
		boolean isAdmin = actor.getRole() != null && "ADMIN".equalsIgnoreCase(actor.getRole().getName());
		if (!isAdmin && (shipment == null || shipment.getCustomer() == null
				|| !Objects.equals(shipment.getCustomer().getId(), actor.getId()))) {
			throw new AccessDeniedException("You are not allowed to access this payment");
		}
	}

	private void verifySignature(byte[] rawBody, String signature) {
		String secret = paymentConfig.getWebhookSecret();
		if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Payment webhook secret is not configured or is too short");
		}
		if (rawBody == null || signature == null || !signature.matches("(?i)[0-9a-f]{64}")) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid payment webhook signature");
		}

		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
			byte[] expected = mac.doFinal(rawBody);
			byte[] received = HexFormat.of().parseHex(signature);
			if (!MessageDigest.isEqual(expected, received)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid payment webhook signature");
			}
		} catch (ResponseStatusException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to verify payment webhook signature", exception);
		}
	}

	private record WebhookPayload(Long paymentId, String status, String transactionCode) {
	}
}
