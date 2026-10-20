package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import vn.uteexpress.entity.Payment;
import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.HubRepository;
import vn.uteexpress.repository.HiddenShipmentRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.PaymentRepository;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.ShipmentEventRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

class ShipmentPaymentAssignmentTest {

	@Test
	void blocksStandaloneShipmentAssignmentUntilPaymentIsPaid() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		Shipment shipment = new Shipment();
		User shipper = new User();
		shipper.setRole(new Role("SHIPPER"));
		shipper.setEnabled(true);

		when(shipmentRepository.findById(5L)).thenReturn(Optional.of(shipment));
		when(userRepository.findById(9L)).thenReturn(Optional.of(shipper));
		when(paymentRepository.existsByShipmentIdAndStatus(5L, "PAID")).thenReturn(false);

		ShipmentService service = new ShipmentService(shipmentRepository, orderRepository, paymentRepository,
				mock(HiddenShipmentRepository.class), userRepository, mock(ShipmentEventRepository.class),
				mock(ReviewRepository.class), mock(HubRepository.class),
				mock(ShippingFeeService.class), mock(NotificationService.class));

		assertThrows(IllegalArgumentException.class, () -> service.assignShipper(5L, 9L));
		verify(shipmentRepository, never()).save(shipment);
	}

	@Test
	void refundsPaidShipmentPaymentWhenDeliveryFails() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		ShipmentEventRepository shipmentEventRepository = mock(ShipmentEventRepository.class);
		HubRepository hubRepository = mock(HubRepository.class);
		NotificationService notificationService = mock(NotificationService.class);
		Shipment shipment = mock(Shipment.class);
		Payment payment = new Payment();
		payment.setStatus("PAID");
		when(shipment.getStatus()).thenReturn(ShipmentStatus.DELIVERING);
		when(shipmentRepository.findById(5L)).thenReturn(Optional.of(shipment));
		when(shipmentRepository.save(shipment)).thenReturn(shipment);
		when(paymentRepository.findFirstByShipmentIdAndStatusOrderByCreatedAtDesc(5L, "PAID"))
				.thenReturn(Optional.of(payment));

		ShipmentService service = new ShipmentService(shipmentRepository, orderRepository, paymentRepository,
				mock(HiddenShipmentRepository.class), userRepository, shipmentEventRepository,
				mock(ReviewRepository.class), hubRepository, mock(ShippingFeeService.class),
				notificationService);

		service.updateStatus(5L, ShipmentStatus.FAILED);

		assertEquals("REFUNDED", payment.getStatus());
		assertNotNull(payment.getUpdatedAt());
		verify(paymentRepository).save(payment);
	}
}
