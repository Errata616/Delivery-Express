package vn.uteexpress.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.security.CurrentUser;
import vn.uteexpress.service.StatisticsService;

@RestController
@RequestMapping("/api")
public class StatisticsController {

	private final StatisticsService statisticsService;
	private final CurrentUser currentUser;

	public StatisticsController(StatisticsService statisticsService, CurrentUser currentUser) {
		this.statisticsService = statisticsService;
		this.currentUser = currentUser;
	}

	@GetMapping("/statistics")
	public ResponseEntity<?> getStatistics() {
		return ResponseEntity.ok(statisticsService.getDashboardStats(currentUser.getUser()));
	}
}
