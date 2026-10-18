package vn.uteexpress.dto;

import java.math.BigDecimal;
import java.util.Map;

import vn.uteexpress.entity.OrderStatus;
import vn.uteexpress.entity.ShipmentStatus;

public class StatisticsResponse {

	private long totalOrders;
	private Map<OrderStatus, Long> ordersByStatus;
	private long totalShipments;
	private Map<ShipmentStatus, Long> shipmentsByStatus;
	private long deliveredToday;
	private BigDecimal deliveredShippingRevenue;
	private BigDecimal pendingCodAmount;
	private long totalCustomers;
	private long totalShippers;

	public long getTotalOrders() {
		return totalOrders;
	}

	public void setTotalOrders(long totalOrders) {
		this.totalOrders = totalOrders;
	}

	public Map<OrderStatus, Long> getOrdersByStatus() {
		return ordersByStatus;
	}

	public void setOrdersByStatus(Map<OrderStatus, Long> ordersByStatus) {
		this.ordersByStatus = ordersByStatus;
	}

	public long getTotalShipments() {
		return totalShipments;
	}

	public void setTotalShipments(long totalShipments) {
		this.totalShipments = totalShipments;
	}

	public Map<ShipmentStatus, Long> getShipmentsByStatus() {
		return shipmentsByStatus;
	}

	public void setShipmentsByStatus(Map<ShipmentStatus, Long> shipmentsByStatus) {
		this.shipmentsByStatus = shipmentsByStatus;
	}

	public long getDeliveredToday() {
		return deliveredToday;
	}

	public void setDeliveredToday(long deliveredToday) {
		this.deliveredToday = deliveredToday;
	}

	public BigDecimal getDeliveredShippingRevenue() {
		return deliveredShippingRevenue;
	}

	public void setDeliveredShippingRevenue(BigDecimal deliveredShippingRevenue) {
		this.deliveredShippingRevenue = deliveredShippingRevenue;
	}

	public BigDecimal getPendingCodAmount() {
		return pendingCodAmount;
	}

	public void setPendingCodAmount(BigDecimal pendingCodAmount) {
		this.pendingCodAmount = pendingCodAmount;
	}

	public long getTotalCustomers() {
		return totalCustomers;
	}

	public void setTotalCustomers(long totalCustomers) {
		this.totalCustomers = totalCustomers;
	}

	public long getTotalShippers() {
		return totalShippers;
	}

	public void setTotalShippers(long totalShippers) {
		this.totalShippers = totalShippers;
	}
}
