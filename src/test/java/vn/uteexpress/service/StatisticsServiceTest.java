package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import vn.uteexpress.dto.StatisticsResponse;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

class StatisticsServiceTest {

	@Test
	void returnsAggregatedDashboardStatistics() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		UserRepository userRepository = mock(UserRepository.class);

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
		when(shipmentRepository.sumCodAmountByStatusIn(org.mockito.ArgumentMatchers.anyList()))
				.thenReturn(new BigDecimal("75000"));
		when(userRepository.countByRoleName("USER")).thenReturn(8L);
		when(userRepository.countByRoleName("SHIPPER")).thenReturn(5L);

		StatisticsResponse response = new StatisticsService(shipmentRepository, orderRepository, userRepository)
				.getDashboardStats();

		assertEquals(12L, response.getTotalOrders());
		assertEquals(2L, response.getOrdersByStatus().get(OrderStatus.PENDING));
		assertEquals(24L, response.getTotalShipments());
		assertEquals(3L, response.getShipmentsByStatus().get(ShipmentStatus.DELIVERED));
		assertEquals(4L, response.getDeliveredToday());
		assertEquals(new BigDecimal("125000.50"), response.getDeliveredShippingRevenue());
		assertEquals(new BigDecimal("75000"), response.getPendingCodAmount());
		assertEquals(8L, response.getTotalCustomers());
		assertEquals(5L, response.getTotalShippers());
	}

	@Test
	void usesZeroWhenNoDeliveredRevenueOrActiveCodExists() {
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		UserRepository userRepository = mock(UserRepository.class);

		StatisticsResponse response = new StatisticsService(shipmentRepository, orderRepository, userRepository)
				.getDashboardStats();

		assertEquals(BigDecimal.ZERO, response.getDeliveredShippingRevenue());
		assertEquals(BigDecimal.ZERO, response.getPendingCodAmount());
	}
}
