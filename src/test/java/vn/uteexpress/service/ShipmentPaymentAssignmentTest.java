package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.HubRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.PaymentRepository;
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
				userRepository, mock(ShipmentEventRepository.class), mock(HubRepository.class),
				mock(ShippingFeeService.class), mock(NotificationService.class));

		assertThrows(IllegalArgumentException.class, () -> service.assignShipper(5L, 9L));
		verify(shipmentRepository, never()).save(shipment);
	}
}
