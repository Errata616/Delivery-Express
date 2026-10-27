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
import vn.uteexpress.entity.User;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.repository.DeletedShipmentRevenueRepository;
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
	private final DeletedShipmentRevenueRepository deletedShipmentRevenueRepository;

	public StatisticsService(ShipmentRepository shipmentRepository, OrderRepository orderRepository,
			UserRepository userRepository, DeletedShipmentRevenueRepository deletedShipmentRevenueRepository) {
		this.shipmentRepository = shipmentRepository;
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.deletedShipmentRevenueRepository = deletedShipmentRevenueRepository;
	}

	@Transactional(readOnly = true)
	public StatisticsResponse getDashboardStats(User currentUser) {
		LocalDate today = LocalDate.now();
		LocalDateTime startOfToday = today.atStartOfDay();
		LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
		boolean isAdmin = currentUser.getRole() != null
				&& "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		Map<OrderStatus, Long> ordersByStatus = new EnumMap<>(OrderStatus.class);
		for (OrderStatus status : OrderStatus.values()) {
			long count = isAdmin ? orderRepository.countByStatus(status)
					: orderRepository.countByShipperManagerIdAndStatus(currentUser.getId(), status);
			ordersByStatus.put(status, count);
		}

		Map<ShipmentStatus, Long> shipmentsByStatus = new EnumMap<>(ShipmentStatus.class);
		for (ShipmentStatus status : ShipmentStatus.values()) {
			long count = isAdmin ? shipmentRepository.countByStatus(status)
					: shipmentRepository.countByShipper_Manager_IdAndStatus(currentUser.getId(), status);
			shipmentsByStatus.put(status, count);
		}

		BigDecimal currentDeliveredRevenue = isAdmin
				? shipmentRepository.sumShippingFeeByStatus(ShipmentStatus.DELIVERED)
				: shipmentRepository.sumShippingFeeByShipperManagerIdAndStatus(currentUser.getId(),
						ShipmentStatus.DELIVERED);
		BigDecimal deletedDeliveredRevenue = isAdmin
				? deletedShipmentRevenueRepository.sumAmount()
				: deletedShipmentRevenueRepository.sumAmountByManagerId(currentUser.getId());
		BigDecimal pendingCod = isAdmin ? shipmentRepository.sumCodAmountByStatusIn(ACTIVE_SHIPMENT_STATUSES)
				: shipmentRepository.sumCodAmountByShipperManagerIdAndStatusIn(currentUser.getId(),
						ACTIVE_SHIPMENT_STATUSES);

		StatisticsResponse response = new StatisticsResponse();
		response.setTotalOrders(isAdmin ? orderRepository.count()
				: orderRepository.countByShipperManagerId(currentUser.getId()));
		response.setOrdersByStatus(ordersByStatus);
		response.setTotalShipments(isAdmin ? shipmentRepository.count()
				: shipmentRepository.countByShipper_Manager_Id(currentUser.getId()));
		response.setShipmentsByStatus(shipmentsByStatus);
		long deliveredToday = isAdmin
				? shipmentRepository.countByStatusAndDeliveredAtGreaterThanEqualAndDeliveredAtLessThan(
						ShipmentStatus.DELIVERED, startOfToday, startOfTomorrow)
				: shipmentRepository.countByShipper_Manager_IdAndStatusAndDeliveredAtGreaterThanEqualAndDeliveredAtLessThan(
						currentUser.getId(), ShipmentStatus.DELIVERED, startOfToday, startOfTomorrow);
		response.setDeliveredToday(deliveredToday);
		response.setDeliveredShippingRevenue(
				zeroIfNull(currentDeliveredRevenue).add(zeroIfNull(deletedDeliveredRevenue)));
		response.setPendingCodAmount(pendingCod == null ? BigDecimal.ZERO : pendingCod);
		response.setTotalCustomers(userRepository.countByRoleName("USER"));
		response.setTotalShippers(userRepository.countByRoleName("SHIPPER"));
		return response;
	}

	private BigDecimal zeroIfNull(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount;
	}
}
