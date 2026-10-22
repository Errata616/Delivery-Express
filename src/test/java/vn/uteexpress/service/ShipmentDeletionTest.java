package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import vn.uteexpress.entity.Order;
import vn.uteexpress.entity.Payment;
import vn.uteexpress.entity.Review;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentEvent;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.repository.HubRepository;
import vn.uteexpress.repository.HiddenShipmentRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.PaymentRepository;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.ShipmentEventRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

class ShipmentDeletionTest {

	@Test
	void currentShipmentsExcludeDeliveredAndRefundedShipments() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		Shipment ready = currentShipment(1L, ShipmentStatus.READY);
		Shipment failed = currentShipment(2L, ShipmentStatus.FAILED);
		Shipment delivered = currentShipment(3L, ShipmentStatus.DELIVERED);
		Shipment refunded = currentShipment(4L, ShipmentStatus.FAILED);
		Payment refund = new Payment();
		refund.setShipment(refunded);
		when(paymentRepository.findByStatusIgnoreCase("REFUNDED")).thenReturn(List.of(refund));
		when(shipmentRepository.findAll()).thenReturn(List.of(delivered, refunded, ready, failed));

		List<Shipment> current = new ShipmentService(shipmentRepository, mock(OrderRepository.class),
				paymentRepository, mock(HiddenShipmentRepository.class), mock(UserRepository.class),
				mock(ShipmentEventRepository.class), mock(ReviewRepository.class), mock(HubRepository.class),
				mock(ShippingFeeService.class), mock(NotificationService.class)).getCurrentShipments();

		assertEquals(List.of(2L, 1L), current.stream().map(Shipment::getId).toList());
	}

	@Test
	void deletesDeliveredShipmentAndItsRelatedRecords() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		HiddenShipmentRepository hiddenShipmentRepository = mock(HiddenShipmentRepository.class);
		ShipmentEventRepository eventRepository = mock(ShipmentEventRepository.class);
		ReviewRepository reviewRepository = mock(ReviewRepository.class);
		Shipment shipment = ownedShipment(ShipmentStatus.DELIVERED);
		Payment payment = new Payment();
		ShipmentEvent event = mock(ShipmentEvent.class);
		Review review = mock(Review.class);

		when(shipmentRepository.findById(9L)).thenReturn(Optional.of(shipment));
		when(paymentRepository.findByShipmentId(9L)).thenReturn(List.of(payment));
		when(eventRepository.findByShipmentIdOrderByCreatedAtDescIdDesc(9L)).thenReturn(List.of(event));
		when(reviewRepository.findByShipmentId(9L)).thenReturn(Optional.of(review));

		new ShipmentService(shipmentRepository, mock(OrderRepository.class), paymentRepository,
				hiddenShipmentRepository, mock(UserRepository.class), eventRepository, reviewRepository,
				mock(HubRepository.class), mock(ShippingFeeService.class), mock(NotificationService.class))
				.deleteCompletedShipment(9L);

		verify(reviewRepository).delete(review);
		verify(eventRepository).deleteAll(List.of(event));
		verify(paymentRepository).deleteAll(List.of(payment));
		verify(hiddenShipmentRepository).deleteByShipmentId(9L);
		verify(shipmentRepository).delete(shipment);
	}

	@Test
	void allowsDeletingFailedShipmentWhenItsPaymentWasRefunded() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		Shipment shipment = ownedShipment(ShipmentStatus.FAILED);
		Order order = mock(Order.class);
		Payment payment = new Payment();
		payment.setStatus("REFUNDED");
		when(shipment.getOrder()).thenReturn(order);
		when(order.getId()).thenReturn(20L);

		when(shipmentRepository.findById(9L)).thenReturn(Optional.of(shipment));
		when(paymentRepository.findByShipmentId(9L)).thenReturn(List.of());
		when(paymentRepository.findByOrderId(20L)).thenReturn(List.of(payment));

		new ShipmentService(shipmentRepository, orderRepository, paymentRepository, mock(HiddenShipmentRepository.class),
				mock(UserRepository.class), mock(ShipmentEventRepository.class), mock(ReviewRepository.class), mock(HubRepository.class),
				mock(ShippingFeeService.class), mock(NotificationService.class)).deleteCompletedShipment(9L);

		verify(shipmentRepository).delete(shipment);
		verify(paymentRepository).deleteAll(List.of(payment));
		verify(orderRepository).delete(order);
	}

	@Test
	void rejectsInTransitShipmentEvenWhenPaid() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		Shipment shipment = ownedShipment(ShipmentStatus.DELIVERING);
		Payment payment = new Payment();
		payment.setStatus("PAID");

		when(shipmentRepository.findById(9L)).thenReturn(Optional.of(shipment));
		when(paymentRepository.findByShipmentId(9L)).thenReturn(List.of(payment));

		assertThrows(IllegalStateException.class, () -> createService(shipmentRepository, paymentRepository,
				mock(ShipmentEventRepository.class), mock(ReviewRepository.class))
				.deleteCompletedShipment(9L));

		verify(shipmentRepository, never()).delete(shipment);
		verify(paymentRepository, never()).deleteAll(List.of(payment));
	}

	private Shipment ownedShipment(ShipmentStatus status) {
		Shipment shipment = mock(Shipment.class);
		when(shipment.getId()).thenReturn(9L);
		when(shipment.getStatus()).thenReturn(status);
		return shipment;
	}

	private Shipment currentShipment(Long id, ShipmentStatus status) {
		Shipment shipment = mock(Shipment.class);
		when(shipment.getId()).thenReturn(id);
		when(shipment.getStatus()).thenReturn(status);
		when(shipment.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 10, 22, 10, 0).plusMinutes(id));
		return shipment;
	}

	private ShipmentService createService(ShipmentRepository shipmentRepository, PaymentRepository paymentRepository,
			ShipmentEventRepository eventRepository, ReviewRepository reviewRepository) {
		return new ShipmentService(shipmentRepository, mock(OrderRepository.class), paymentRepository,
				mock(HiddenShipmentRepository.class), mock(UserRepository.class), eventRepository, reviewRepository, mock(HubRepository.class),
				mock(ShippingFeeService.class), mock(NotificationService.class));
	}
}
