package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import vn.uteexpress.entity.PaymentMethod;
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
import vn.uteexpress.repository.ShipperZoneRepository;
import vn.uteexpress.repository.UserRepository;

class ShipmentCancellationRefundTest {

	@Test
	void cancellingWalletPaidShipmentRefundsWalletAndMovesItOutOfCurrentList() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		PaymentRepository paymentRepository = mock(PaymentRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		Shipment shipment = mock(Shipment.class);
		User customer = mock(User.class);
		User lockedCustomer = mock(User.class);
		BigDecimal shippingFee = new BigDecimal("25000");

		when(shipment.getId()).thenReturn(11L);
		when(shipment.getStatus()).thenReturn(ShipmentStatus.READY, ShipmentStatus.CANCELLED);
		when(shipment.getPaymentStatus()).thenReturn("PAID");
		when(shipment.getPaymentMethod()).thenReturn(PaymentMethod.WALLET);
		when(shipment.getCustomer()).thenReturn(customer);
		when(shipment.getShippingFee()).thenReturn(shippingFee);
		when(customer.getId()).thenReturn(7L);
		when(lockedCustomer.getWalletBalance()).thenReturn(new BigDecimal("10000"));
		when(shipmentRepository.findById(11L)).thenReturn(Optional.of(shipment));
		when(shipmentRepository.save(shipment)).thenReturn(shipment);
		when(shipmentRepository.findAll()).thenReturn(List.of(shipment));
		when(paymentRepository.findFirstByShipmentIdAndStatusOrderByCreatedAtDesc(11L, "PAID"))
				.thenReturn(Optional.empty());
		when(paymentRepository.findByStatusIgnoreCase("REFUNDED")).thenReturn(List.of());
		when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(lockedCustomer));

		ShipmentService service = new ShipmentService(shipmentRepository, mock(OrderRepository.class),
				paymentRepository, mock(HiddenShipmentRepository.class), userRepository,
				mock(ShipmentEventRepository.class), mock(ReviewRepository.class), mock(HubRepository.class),
				mock(ShippingFeeService.class), mock(NotificationService.class), mock(ShipperZoneRepository.class),
				mock(RouteService.class));

		service.updateStatus(11L, ShipmentStatus.CANCELLED);

		verify(lockedCustomer).setWalletBalance(new BigDecimal("35000"));
		verify(userRepository).save(lockedCustomer);
		verify(shipment).setPaymentStatus("REFUNDED");
		assertTrue(service.getCurrentShipments().isEmpty());
		assertEquals(List.of(shipment), service.getDeletableShipments());
	}
}
