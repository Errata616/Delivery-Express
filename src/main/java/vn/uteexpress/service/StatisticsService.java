package vn.uteexpress.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.dto.StatisticsResponse;
import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class StatisticsService {

	private static final List<ShipmentStatus> ACTIVE_SHIPMENT_STATUSES = List.of(ShipmentStatus.READY,
			ShipmentStatus.ASSIGNED, ShipmentStatus.PICKED_UP, ShipmentStatus.DELIVERING);

	private final ShipmentRepository shipmentRepository;
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;

	public StatisticsService(ShipmentRepository shipmentRepository, OrderRepository orderRepository,
			UserRepository userRepository) {
		this.shipmentRepository = shipmentRepository;
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public StatisticsResponse getDashboardStats() {
		LocalDate today = LocalDate.now();
		LocalDateTime startOfToday = today.atStartOfDay();
		LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

		Map<OrderStatus, Long> ordersByStatus = new EnumMap<>(OrderStatus.class);
		for (OrderStatus status : OrderStatus.values()) {
			ordersByStatus.put(status, orderRepository.countByStatus(status));
		}

		Map<ShipmentStatus, Long> shipmentsByStatus = new EnumMap<>(ShipmentStatus.class);
		for (ShipmentStatus status : ShipmentStatus.values()) {
			shipmentsByStatus.put(status, shipmentRepository.countByStatus(status));
		}

		BigDecimal deliveredRevenue = shipmentRepository.sumShippingFeeByStatus(ShipmentStatus.DELIVERED);
		BigDecimal pendingCod = shipmentRepository.sumCodAmountByStatusIn(ACTIVE_SHIPMENT_STATUSES);

		StatisticsResponse response = new StatisticsResponse();
		response.setTotalOrders(orderRepository.count());
		response.setOrdersByStatus(ordersByStatus);
		response.setTotalShipments(shipmentRepository.count());
		response.setShipmentsByStatus(shipmentsByStatus);
		response.setDeliveredToday(shipmentRepository
				.countByStatusAndDeliveredAtGreaterThanEqualAndDeliveredAtLessThan(ShipmentStatus.DELIVERED,
						startOfToday, startOfTomorrow));
		response.setDeliveredShippingRevenue(deliveredRevenue == null ? BigDecimal.ZERO : deliveredRevenue);
		response.setPendingCodAmount(pendingCod == null ? BigDecimal.ZERO : pendingCod);
		response.setTotalCustomers(userRepository.countByRoleName("USER"));
		response.setTotalShippers(userRepository.countByRoleName("SHIPPER"));
		return response;
	}
}
