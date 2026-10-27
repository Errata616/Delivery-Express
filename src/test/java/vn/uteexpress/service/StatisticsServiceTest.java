package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import vn.uteexpress.dto.StatisticsResponse;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.DeletedShipmentRevenueRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

class StatisticsServiceTest {

	@Test
	void returnsAggregatedDashboardStatistics() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		DeletedShipmentRevenueRepository deletedRevenueRepository = mock(DeletedShipmentRevenueRepository.class);

		when(orderRepository.count()).thenReturn(12L);
		for (OrderStatus status : OrderStatus.values()) {
			when(orderRepository.countByStatus(status)).thenReturn(2L);
		}
		when(shipmentRepository.count()).thenReturn(24L);
		for (ShipmentStatus status : ShipmentStatus.values()) {
			when(shipmentRepository.countByStatus(status)).thenReturn(3L);
		}
		when(shipmentRepository.countByStatusAndDeliveredAtGreaterThanEqualAndDeliveredAtLessThan(
				org.mockito.ArgumentMatchers.eq(ShipmentStatus.DELIVERED),
				org.mockito.ArgumentMatchers.any(LocalDateTime.class),
				org.mockito.ArgumentMatchers.any(LocalDateTime.class))).thenReturn(4L);
		when(shipmentRepository.sumShippingFeeByStatus(ShipmentStatus.DELIVERED))
				.thenReturn(new BigDecimal("125000.50"));
		when(deletedRevenueRepository.sumAmount()).thenReturn(new BigDecimal("5000"));
		when(shipmentRepository.sumCodAmountByStatusIn(org.mockito.ArgumentMatchers.anyList()))
				.thenReturn(new BigDecimal("75000"));
		when(userRepository.countByRoleName("USER")).thenReturn(8L);
		when(userRepository.countByRoleName("SHIPPER")).thenReturn(5L);

		User admin = mock(User.class);
		when(admin.getRole()).thenReturn(new Role("ADMIN"));

		StatisticsResponse response = new StatisticsService(shipmentRepository, orderRepository, userRepository,
				deletedRevenueRepository)
				.getDashboardStats(admin);

		assertEquals(12L, response.getTotalOrders());
		assertEquals(2L, response.getOrdersByStatus().get(OrderStatus.PENDING));
		assertEquals(24L, response.getTotalShipments());
		assertEquals(3L, response.getShipmentsByStatus().get(ShipmentStatus.DELIVERED));
		assertEquals(4L, response.getDeliveredToday());
		assertEquals(new BigDecimal("130000.50"), response.getDeliveredShippingRevenue());
		assertEquals(new BigDecimal("75000"), response.getPendingCodAmount());
		assertEquals(8L, response.getTotalCustomers());
		assertEquals(5L, response.getTotalShippers());
	}

	@Test
	void managersOnlySeeOrdersAssignedToTheirShippers() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		DeletedShipmentRevenueRepository deletedRevenueRepository = mock(DeletedShipmentRevenueRepository.class);
		User manager = mock(User.class);
		when(manager.getId()).thenReturn(7L);
		when(manager.getRole()).thenReturn(new Role("MANAGER"));
		when(orderRepository.countByShipperManagerId(7L)).thenReturn(6L);
		for (OrderStatus status : OrderStatus.values()) {
			when(orderRepository.countByShipperManagerIdAndStatus(7L, status)).thenReturn(1L);
		}
		when(shipmentRepository.countByShipper_Manager_Id(7L)).thenReturn(13L);
		for (ShipmentStatus status : ShipmentStatus.values()) {
			when(shipmentRepository.countByShipper_Manager_IdAndStatus(7L, status)).thenReturn(2L);
		}
		when(shipmentRepository.countByShipper_Manager_IdAndStatusAndDeliveredAtGreaterThanEqualAndDeliveredAtLessThan(
				org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq(ShipmentStatus.DELIVERED),
				org.mockito.ArgumentMatchers.any(LocalDateTime.class),
				org.mockito.ArgumentMatchers.any(LocalDateTime.class))).thenReturn(1L);
		when(shipmentRepository.sumShippingFeeByShipperManagerIdAndStatus(7L, ShipmentStatus.DELIVERED))
				.thenReturn(new BigDecimal("2500"));
		when(deletedRevenueRepository.sumAmountByManagerId(7L)).thenReturn(new BigDecimal("1000"));
		when(shipmentRepository.sumCodAmountByShipperManagerIdAndStatusIn(org.mockito.ArgumentMatchers.eq(7L),
				org.mockito.ArgumentMatchers.anyList())).thenReturn(new BigDecimal("700"));

		StatisticsResponse response = new StatisticsService(shipmentRepository, orderRepository, userRepository,
				deletedRevenueRepository)
				.getDashboardStats(manager);

		assertEquals(6L, response.getTotalOrders());
		assertEquals(1L, response.getOrdersByStatus().get(OrderStatus.PENDING));
		assertEquals(13L, response.getTotalShipments());
		assertEquals(2L, response.getShipmentsByStatus().get(ShipmentStatus.DELIVERED));
		assertEquals(1L, response.getDeliveredToday());
		assertEquals(new BigDecimal("3500"), response.getDeliveredShippingRevenue());
		assertEquals(new BigDecimal("700"), response.getPendingCodAmount());
		org.mockito.Mockito.verify(shipmentRepository).countByShipper_Manager_Id(7L);
		org.mockito.Mockito.verify(shipmentRepository, org.mockito.Mockito.never()).count();
		org.mockito.Mockito.verify(shipmentRepository, org.mockito.Mockito.never())
				.countByStatus(org.mockito.ArgumentMatchers.any());
		org.mockito.Mockito.verify(orderRepository).countByShipperManagerId(7L);
		org.mockito.Mockito.verify(orderRepository, org.mockito.Mockito.never()).count();
		org.mockito.Mockito.verify(orderRepository, org.mockito.Mockito.never())
				.countByStatus(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void usesZeroWhenNoDeliveredRevenueOrActiveCodExists() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		DeletedShipmentRevenueRepository deletedRevenueRepository = mock(DeletedShipmentRevenueRepository.class);
		User admin = mock(User.class);
		when(admin.getRole()).thenReturn(new Role("ADMIN"));

		StatisticsResponse response = new StatisticsService(shipmentRepository, orderRepository, userRepository,
				deletedRevenueRepository)
				.getDashboardStats(admin);

		assertEquals(BigDecimal.ZERO, response.getDeliveredShippingRevenue());
		assertEquals(BigDecimal.ZERO, response.getPendingCodAmount());
	}
}
