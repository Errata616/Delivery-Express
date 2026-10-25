package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;

import vn.uteexpress.entity.HiddenShipment;
import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.HiddenShipmentRepository;
import vn.uteexpress.repository.HubRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.PaymentRepository;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.ShipmentEventRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.ShipperZoneRepository;
import vn.uteexpress.repository.UserRepository;

class ShipmentViewRemovalTest {

	@Test
	void allowsOwnerToHideShipmentWithoutDeletingIt() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		HiddenShipmentRepository hiddenRepository = mock(HiddenShipmentRepository.class);
		Shipment shipment = mock(Shipment.class);
		User user = user(7L, "USER");
		when(shipment.getId()).thenReturn(12L);
		when(shipment.getCustomer()).thenReturn(user);
		when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));
		when(hiddenRepository.findByUserIdAndShipmentId(7L, 12L)).thenReturn(Optional.empty());

		createService(shipmentRepository, hiddenRepository).hideShipmentFromView(12L, user);

		verify(hiddenRepository).save(org.mockito.ArgumentMatchers.any(HiddenShipment.class));
		verify(shipmentRepository, never()).delete(shipment);
	}

	@Test
	void allowsAssignedShipperAndManagerToHideFromOwnView() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		HiddenShipmentRepository hiddenRepository = mock(HiddenShipmentRepository.class);
		Shipment shipment = mock(Shipment.class);
		User shipper = user(8L, "SHIPPER");
		when(shipment.getShipper()).thenReturn(shipper);
		when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));
		when(hiddenRepository.findByUserIdAndShipmentId(8L, 12L)).thenReturn(Optional.empty());

		createService(shipmentRepository, hiddenRepository).hideShipmentFromView(12L, shipper);

		verify(hiddenRepository).save(org.mockito.ArgumentMatchers.any(HiddenShipment.class));

		User manager = user(9L, "MANAGER");
		when(hiddenRepository.findByUserIdAndShipmentId(9L, 12L)).thenReturn(Optional.empty());
		createService(shipmentRepository, hiddenRepository).hideShipmentFromView(12L, manager);
		verify(hiddenRepository, times(2)).save(org.mockito.ArgumentMatchers.any(HiddenShipment.class));
	}

	@Test
	void preventsNonOwnersAndAdminsFromHidingShipments() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		HiddenShipmentRepository hiddenRepository = mock(HiddenShipmentRepository.class);
		Shipment shipment = mock(Shipment.class);
		when(shipmentRepository.findById(12L)).thenReturn(Optional.of(shipment));

		assertThrows(IllegalArgumentException.class,
				() -> createService(shipmentRepository, hiddenRepository).hideShipmentFromView(12L, user(7L, "USER")));
		assertThrows(IllegalArgumentException.class,
				() -> createService(shipmentRepository, hiddenRepository).hideShipmentFromView(12L, user(7L, "ADMIN")));

		verify(hiddenRepository, never()).save(org.mockito.ArgumentMatchers.any(HiddenShipment.class));
	}

	@Test
	void filtersHiddenShipmentOnlyFromThatUsersList() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		HiddenShipmentRepository hiddenRepository = mock(HiddenShipmentRepository.class);
		User user = user(7L, "USER");
		Shipment hidden = mock(Shipment.class);
		Shipment visible = mock(Shipment.class);
		when(hidden.getId()).thenReturn(12L);
		when(visible.getId()).thenReturn(13L);
		when(hiddenRepository.findByUserId(7L)).thenReturn(List.of(new HiddenShipment(user, hidden)));

		List<Shipment> result = createService(shipmentRepository, hiddenRepository)
				.filterHiddenShipments(7L, List.of(hidden, visible));

		assertEquals(List.of(visible), result);
	}

	private User user(Long id, String roleName) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(id);
		when(user.getRole()).thenReturn(new Role(roleName));
		return user;
	}

	private ShipmentService createService(ShipmentRepository shipmentRepository,
			HiddenShipmentRepository hiddenRepository) {
		return new ShipmentService(shipmentRepository, mock(OrderRepository.class), mock(PaymentRepository.class),
				hiddenRepository, mock(UserRepository.class), mock(ShipmentEventRepository.class),
				mock(ReviewRepository.class), mock(HubRepository.class), mock(ShippingFeeService.class),
				mock(NotificationService.class), mock(ShipperZoneRepository.class), mock(RouteService.class));
	}
}
