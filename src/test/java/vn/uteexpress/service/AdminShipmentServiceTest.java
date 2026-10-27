package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import vn.uteexpress.entity.DeletedShipmentRevenue;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.DeletedShipmentRevenueRepository;

class AdminShipmentServiceTest {

	@Test
	void archivesDeliveredShippingRevenueBeforeDeletingShipment() {
		ShipmentService shipmentService = mock(ShipmentService.class);
		DeletedShipmentRevenueRepository revenueRepository = mock(DeletedShipmentRevenueRepository.class);
		Shipment shipment = mock(Shipment.class);
		User shipper = mock(User.class);
		User manager = mock(User.class);

		when(shipment.getStatus()).thenReturn(ShipmentStatus.DELIVERED);
		when(shipment.getShippingFee()).thenReturn(new BigDecimal("55000"));
		when(shipment.getShipper()).thenReturn(shipper);
		when(shipper.getManager()).thenReturn(manager);
		when(manager.getId()).thenReturn(17L);
		when(shipmentService.getById(14L)).thenReturn(shipment);

		new AdminShipmentService(shipmentService, revenueRepository).deleteCompletedShipment(14L);

		ArgumentCaptor<DeletedShipmentRevenue> archivedRevenue = ArgumentCaptor.forClass(DeletedShipmentRevenue.class);
		verify(revenueRepository).save(archivedRevenue.capture());
		assertEquals(new BigDecimal("55000"), archivedRevenue.getValue().getAmount());
		assertEquals(17L, archivedRevenue.getValue().getManagerId());
		verify(shipmentService).deleteCompletedShipment(14L);
	}

	@Test
	void doesNotArchiveRevenueForShipmentThatWasNotDelivered() {
		ShipmentService shipmentService = mock(ShipmentService.class);
		DeletedShipmentRevenueRepository revenueRepository = mock(DeletedShipmentRevenueRepository.class);
		Shipment shipment = mock(Shipment.class);

		when(shipment.getStatus()).thenReturn(ShipmentStatus.CANCELLED);
		when(shipmentService.getById(14L)).thenReturn(shipment);

		new AdminShipmentService(shipmentService, revenueRepository).deleteCompletedShipment(14L);

		verify(revenueRepository, org.mockito.Mockito.never()).save(any(DeletedShipmentRevenue.class));
		verify(shipmentService).deleteCompletedShipment(14L);
	}
}
