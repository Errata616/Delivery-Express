package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import vn.uteexpress.dto.BookingRequest;
import vn.uteexpress.repository.HubRepository;
import vn.uteexpress.repository.HiddenShipmentRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.PaymentRepository;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.ShipmentEventRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.ShipperZoneRepository;
import vn.uteexpress.repository.UserRepository;

class ShipmentBookingValidationTest {

	@Test
	void rejectsUnrecognizedDistrictsBeforeCreatingShipment() {
		RouteService routeService = mock(RouteService.class);
		UserRepository userRepository = mock(UserRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		BookingRequest request = new BookingRequest();
		request.setPickupDistrict("Địa chỉ không tồn tại");
		request.setDeliveryDistrict("Quận 1");

		when(routeService.plan("Địa chỉ không tồn tại", "Quận 1"))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Địa chỉ không đúng"));

		ShipmentService service = new ShipmentService(
				shipmentRepository,
				mock(OrderRepository.class),
				mock(PaymentRepository.class),
				mock(HiddenShipmentRepository.class),
				userRepository,
				mock(ShipmentEventRepository.class),
				mock(ReviewRepository.class),
				mock(HubRepository.class),
				mock(ShippingFeeService.class),
				mock(NotificationService.class),
				mock(ShipperZoneRepository.class),
				routeService);

		assertThrows(ResponseStatusException.class, () -> service.createBooking(1L, request));

		verify(routeService).plan("Địa chỉ không tồn tại", "Quận 1");
		verifyNoInteractions(userRepository, shipmentRepository);
	}
}
