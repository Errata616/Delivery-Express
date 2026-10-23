package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.HexFormat;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.ObjectMapper;

import vn.uteexpress.config.PaymentConfig;
import vn.uteexpress.dto.PaymentRequest;
import vn.uteexpress.dto.PaymentResponse;
import vn.uteexpress.entity.Payment;
import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.PaymentRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.security.CurrentUser;

class PaymentServiceTest {

	private static final String WEBHOOK_SECRET = "test-webhook-secret-with-32-bytes";

	@Test
	void createsPendingPaymentUsingTheShipmentFee() {
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		User customer = customer();
		Shipment shipment = mock(Shipment.class);
		when(shipment.getId()).thenReturn(12L);
		when(shipment.getCustomer()).thenReturn(customer);
		when(shipment.getShippingFee()).thenReturn(new BigDecimal("25000"));
		when(shipment.getStatus()).thenReturn(ShipmentStatus.READY);

		when(currentUser.getUser()).thenReturn(customer);
		when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));
		when(paymentRepository.findFirstByShipmentIdAndStatusOrderByCreatedAtDesc(12L, "PENDING"))
				.thenReturn(Optional.empty());
		when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PaymentResponse response = service(paymentRepository, shipmentRepository, currentUser)
				.createPayment(new PaymentRequest(12L));

		assertEquals(new BigDecimal("25000"), response.amount());
		assertEquals("VND", response.currency());
		assertEquals("SIMULATED", response.provider());
		assertEquals("PENDING", response.status());
	}

	@Test
	void verifiesWebhookAndMarksPaymentPaidIdempotently() throws Exception {
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		Payment payment = new Payment();
		payment.setProvider("SIMULATED");
		payment.setStatus("PENDING");
		when(paymentRepository.findById(7L)).thenReturn(Optional.of(payment));
		when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PaymentService service = service(paymentRepository, shipmentRepository, currentUser);
		byte[] payload = "{\"paymentId\":7,\"status\":\"PAID\",\"transactionCode\":\"SIM-7\"}"
				.getBytes(StandardCharsets.UTF_8);
		String signature = sign(payload);

		PaymentResponse result = service.handleWebhook(payload, signature);
		PaymentResponse duplicate = service.handleWebhook(payload, signature);

		assertEquals("PAID", result.status());
		assertEquals("SIM-7", result.transactionCode());
		assertEquals("PAID", duplicate.status());
		verify(paymentRepository, times(1)).save(payment);
	}

	@Test
	void rejectsUnsignedWebhookBeforeLookingUpPayment() {
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		PaymentService service = service(paymentRepository, shipmentRepository, currentUser);
		byte[] payload = "{\"paymentId\":7,\"status\":\"PAID\"}".getBytes(StandardCharsets.UTF_8);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.handleWebhook(payload, "invalid"));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		verify(paymentRepository, times(0)).findById(7L);
	}

	private PaymentService service(PaymentRepository paymentRepository, ShipmentRepository shipmentRepository,
			CurrentUser currentUser) {
		PaymentConfig paymentConfig = new PaymentConfig();
		paymentConfig.setWebhookSecret(WEBHOOK_SECRET);
		return new PaymentService(paymentRepository, shipmentRepository, currentUser, paymentConfig,
				new ObjectMapper());
	}

	private User customer() {
		User user = new User();
		user.setRole(new Role("USER"));
		return user;
	}

	private String sign(byte[] payload) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return HexFormat.of().formatHex(mac.doFinal(payload));
	}
}
