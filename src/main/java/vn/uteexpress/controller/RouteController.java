package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.dto.RouteResponse;
import vn.uteexpress.routing.DistrictGraph.District;
import vn.uteexpress.service.RouteService;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

	private final RouteService routeService;

	public RouteController(RouteService routeService) {
		this.routeService = routeService;
	}

	@GetMapping("/districts")
	public ResponseEntity<ApiResponse<List<District>>> districts() {
		return ResponseEntity.ok(ApiResponse.ok(routeService.districts()));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<RouteResponse>> route(@RequestParam String from, @RequestParam String to) {
		return ResponseEntity.ok(ApiResponse.ok(routeService.plan(from, to)));
	}
}