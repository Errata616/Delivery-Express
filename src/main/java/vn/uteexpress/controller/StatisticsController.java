package vn.uteexpress.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.service.StatisticsService;

@RestController
@RequestMapping("/api")
public class StatisticsController {

	private final StatisticsService statisticsService;

	public StatisticsController(StatisticsService statisticsService) {
		this.statisticsService = statisticsService;
	}

	@GetMapping("/statistics")
	public ResponseEntity<?> getStatistics() {
		return ResponseEntity.ok(statisticsService.getDashboardStats());
	}
}
