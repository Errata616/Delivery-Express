package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;

import vn.uteexpress.entity.Hub;
import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentEvent;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.ShipperZone;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.HubRepository;
import vn.uteexpress.repository.HiddenShipmentRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.PaymentRepository;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.ShipmentEventRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.ShipperZoneRepository;
import vn.uteexpress.repository.ShippingFeeRuleRepository;
import vn.uteexpress.repository.UserRepository;

class ShipmentRoutingHandoffTest {

	@Test
	void plansThroughActiveZonesAndHandsOffAtNextShipperDistrict() {
		ShipmentRepository shipments = mock(ShipmentRepository.class);
		PaymentRepository payments = mock(PaymentRepository.class);
		UserRepository users = mock(UserRepository.class);
		ShipperZoneRepository zones = mock(ShipperZoneRepository.class);
		HubRepository hubs = mock(HubRepository.class);
		ShipmentEventRepository events = mock(ShipmentEventRepository.class);
		User shipperA = shipper(1L, "Shipper A");
		User shipperB = shipper(2L, "Shipper B");
		Hub q2Hub = new Hub("HCM-Q2", "Bưu cục Quận 2", "Địa chỉ bưu cục", "Quận 2",
				"TP. Hồ Chí Minh", "0900000000");
		Shipment shipment = new Shipment();
		shipment.setPickupProvince("TP. Hồ Chí Minh");
		shipment.setPickupDistrict("Bình Thạnh");
		shipment.setDeliveryProvince("TP. Hồ Chí Minh");
		shipment.setDeliveryDistrict("Quận 7");

		when(shipments.findById(10L)).thenReturn(Optional.of(shipment));
		when(shipments.save(shipment)).thenReturn(shipment);
		when(users.findById(1L)).thenReturn(Optional.of(shipperA));
		when(users.findByRoleName("SHIPPER")).thenReturn(List.of(shipperA, shipperB));
		when(zones.findByShipperIdIn(List.of(1L, 2L))).thenReturn(List.of(
				new ShipperZone(shipperA, "BTH"),
				new ShipperZone(shipperB, "Q7")));
		when(hubs.findByActiveTrue()).thenReturn(List.of(q2Hub));
		when(payments.existsByShipmentIdAndStatus(10L, "PAID")).thenReturn(true);

		ShipmentService service = new ShipmentService(shipments, mock(OrderRepository.class), payments,
				mock(HiddenShipmentRepository.class), users, events,
				mock(ReviewRepository.class), hubs, new ShippingFeeService(mock(ShippingFeeRuleRepository.class)),
				mock(NotificationService.class), zones, new RouteService());

		service.assignShipper(10L, 1L);
		assertEquals(List.of("BTH", "Q2", "Q7"), shipment.getPlannedRouteDistricts());

		service.updateStatus(10L, ShipmentStatus.PICKED_UP);
		service.updateShipmentDistrict(1L, 10L, "Quận 2");
		assertEquals(1L, shipment.getShipper().getId());
		assertEquals(ShipmentStatus.DELIVERING, shipment.getStatus());

		service.updateShipmentDistrict(1L, 10L, "Quận 7");
		assertEquals(2L, shipment.getShipper().getId());
		assertEquals("Q7", shipment.getCurrentDistrict());
		assertEquals(2, shipment.getCurrentRouteIndex());

		ArgumentCaptor<ShipmentEvent> eventCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);
		verify(events, atLeastOnce()).save(eventCaptor.capture());
		assertTrue(eventCaptor.getAllValues().stream()
				.anyMatch(event -> event.getNote().contains("Đã đến Quận 2") && event.getHub() == q2Hub));

		service.updateStatus(10L, ShipmentStatus.DELIVERED);
		assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
	}

	@Test
	void reroutesAroundDistrictsWhoseOnlyShippersAreUnavailable() {
		ShipmentRepository shipments = mock(ShipmentRepository.class);
		PaymentRepository payments = mock(PaymentRepository.class);
		UserRepository users = mock(UserRepository.class);
		ShipperZoneRepository zones = mock(ShipperZoneRepository.class);
		User shipperA = shipper(1L, "Shipper A");
		User shipperB = shipper(2L, "Shipper B");
		User restingShipper = shipper(3L, "Shipper nghỉ");
		when(restingShipper.isAvailable()).thenReturn(false);
		Shipment shipment = new Shipment();
		shipment.setPickupProvince("TP. Hồ Chí Minh");
		shipment.setPickupDistrict("Bình Thạnh");
		shipment.setDeliveryProvince("TP. Hồ Chí Minh");
		shipment.setDeliveryDistrict("Quận 7");

		when(shipments.findById(10L)).thenReturn(Optional.of(shipment));
		when(shipments.save(shipment)).thenReturn(shipment);
		when(users.findById(1L)).thenReturn(Optional.of(shipperA));
		when(users.findByRoleName("SHIPPER")).thenReturn(List.of(shipperA, shipperB, restingShipper));
		when(zones.findByShipperIdIn(List.of(1L, 2L, 3L))).thenReturn(List.of(
				new ShipperZone(shipperA, "BTH"),
				new ShipperZone(shipperB, "Q7"),
				new ShipperZone(restingShipper, "Q2")));
		when(payments.existsByShipmentIdAndStatus(10L, "PAID")).thenReturn(true);

		ShipmentService service = new ShipmentService(shipments, mock(OrderRepository.class), payments,
				mock(HiddenShipmentRepository.class), users, mock(ShipmentEventRepository.class),
				mock(ReviewRepository.class), mock(HubRepository.class), mock(ShippingFeeService.class),
				mock(NotificationService.class), zones, new RouteService());

		service.assignShipper(10L, 1L);
		assertEquals(List.of("BTH", "Q1", "Q4", "Q7"), shipment.getPlannedRouteDistricts());
	}

	@Test
	void assignsShipmentWhenAnotherShipperSharingItsZonesIsUnavailable() {
		ShipmentRepository shipments = mock(ShipmentRepository.class);
		PaymentRepository payments = mock(PaymentRepository.class);
		UserRepository users = mock(UserRepository.class);
		ShipperZoneRepository zones = mock(ShipperZoneRepository.class);
		User activeShipper = shipper(1L, "Shipper H");
		User restingShipper = shipper(2L, "Shipper P");
		when(restingShipper.isAvailable()).thenReturn(false);
		Shipment shipment = new Shipment();
		shipment.setPickupProvince("TP. Hồ Chí Minh");
		shipment.setPickupDistrict("Bình Thạnh");
		shipment.setDeliveryProvince("TP. Hồ Chí Minh");
		shipment.setDeliveryDistrict("Quận 9");

		when(shipments.findById(10L)).thenReturn(Optional.of(shipment));
		when(shipments.save(shipment)).thenReturn(shipment);
		when(users.findById(1L)).thenReturn(Optional.of(activeShipper));
		when(users.findByRoleName("SHIPPER")).thenReturn(List.of(activeShipper, restingShipper));
		when(zones.findByShipperIdIn(List.of(1L, 2L))).thenReturn(List.of(
				new ShipperZone(activeShipper, "BTH"),
				new ShipperZone(activeShipper, "Q2"),
				new ShipperZone(activeShipper, "Q9"),
				new ShipperZone(restingShipper, "BTH"),
				new ShipperZone(restingShipper, "Q2"),
				new ShipperZone(restingShipper, "Q9")));
		when(payments.existsByShipmentIdAndStatus(10L, "PAID")).thenReturn(true);

		ShipmentService service = new ShipmentService(shipments, mock(OrderRepository.class), payments,
				mock(HiddenShipmentRepository.class), users, mock(ShipmentEventRepository.class),
				mock(ReviewRepository.class), mock(HubRepository.class), mock(ShippingFeeService.class),
				mock(NotificationService.class), zones, new RouteService());

		service.assignShipper(10L, 1L);

		assertEquals(activeShipper, shipment.getShipper());
		assertEquals("Q9", shipment.getPlannedRouteDistricts().getLast());
	}

	@Test
	void keepsThePlannedRouteWhenAShipperBecomesUnavailableMidJourney() {
		ShipmentRepository shipments = mock(ShipmentRepository.class);
		PaymentRepository payments = mock(PaymentRepository.class);
		UserRepository users = mock(UserRepository.class);
		ShipperZoneRepository zones = mock(ShipperZoneRepository.class);
		User shipperA = shipper(1L, "Shipper A");
		User shipperB = shipper(2L, "Shipper B");
		User shipperInQ2 = shipper(3L, "Shipper Q2");
		when(shipperInQ2.isAvailable()).thenReturn(true, false);
		Shipment shipment = new Shipment();
		shipment.setPickupProvince("TP. Hồ Chí Minh");
		shipment.setPickupDistrict("Bình Thạnh");
		shipment.setDeliveryProvince("TP. Hồ Chí Minh");
		shipment.setDeliveryDistrict("Quận 7");
		when(shipments.findById(10L)).thenReturn(Optional.of(shipment));
		when(shipments.save(shipment)).thenReturn(shipment);
		when(users.findById(1L)).thenReturn(Optional.of(shipperA));
		when(users.findByRoleName("SHIPPER")).thenReturn(List.of(shipperA, shipperB, shipperInQ2));
		when(zones.findByShipperIdIn(List.of(1L, 2L, 3L))).thenReturn(List.of(
				new ShipperZone(shipperA, "BTH"),
				new ShipperZone(shipperB, "Q7"),
				new ShipperZone(shipperInQ2, "Q2")));
		when(payments.existsByShipmentIdAndStatus(10L, "PAID")).thenReturn(true);

		ShipmentService service = new ShipmentService(shipments, mock(OrderRepository.class), payments,
				mock(HiddenShipmentRepository.class), users, mock(ShipmentEventRepository.class),
				mock(ReviewRepository.class), mock(HubRepository.class), mock(ShippingFeeService.class),
				mock(NotificationService.class), zones, new RouteService());
		service.assignShipper(10L, 1L);
		assertEquals(List.of("BTH", "Q2", "Q7"), shipment.getPlannedRouteDistricts());

		service.updateStatus(10L, ShipmentStatus.PICKED_UP);
		service.updateShipmentDistrict(1L, 10L, "Q2");

		assertEquals(List.of("BTH", "Q2", "Q7"), shipment.getPlannedRouteDistricts());
		assertEquals(1, shipment.getCurrentRouteIndex());
		assertEquals("Q2", shipment.getCurrentDistrict());
		assertEquals(shipperA, shipment.getShipper());
	}

	private User shipper(Long id, String name) {
		User shipper = mock(User.class);
		when(shipper.getId()).thenReturn(id);
		when(shipper.getRole()).thenReturn(new Role("SHIPPER"));
		when(shipper.getFullName()).thenReturn(name);
		when(shipper.isEnabled()).thenReturn(true);
		when(shipper.isAvailable()).thenReturn(true);
		return shipper;
	}
}
