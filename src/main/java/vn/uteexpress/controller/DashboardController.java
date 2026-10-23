package vn.uteexpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

	@GetMapping("/admin/dashboard")
	public String adminDashboard() {
		return "admin/dashboard";
	}

	@GetMapping("/manager/dashboard")
	public String managerDashboard() {
		return "manager/dashboard";
	}

	@GetMapping("/shipper/dashboard")
	public String shipperDashboard() {
		return "shipper/dashboard";
	}

	@GetMapping("/my-orders")
	public String myOrders() {
		return "my-orders";
	}

	@GetMapping("/booking")
	public String booking() {
		return "booking";
	}
}