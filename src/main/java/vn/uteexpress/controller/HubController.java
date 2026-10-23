package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Hub;
import vn.uteexpress.service.HubService;

@RestController
@RequestMapping("/api/hubs")
public class HubController {

	private final HubService hubService;

	public HubController(HubService hubService) {
		this.hubService = hubService;
	}

	// List active hubs. Optional filter: /api/hubs?province=Ho Chi Minh
	@GetMapping
	public ResponseEntity<List<Hub>> getHubs(@RequestParam(required = false) String province) {

		return ResponseEntity.ok(hubService.getHubs(province));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Hub> getHub(@PathVariable Long id) {

		return ResponseEntity.ok(hubService.getHub(id));
	}

	@PostMapping
	public ResponseEntity<Hub> createHub(@RequestBody Hub hub) {

		return ResponseEntity.status(HttpStatus.CREATED).body(hubService.createHub(hub));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Hub> updateHub(@PathVariable Long id, @RequestBody Hub hub) {

		return ResponseEntity.ok(hubService.updateHub(id, hub));
	}

	// Soft delete: marks the hub inactive instead of removing the row, because
	// shipments will reference hubs and a hard delete would break them.
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deactivateHub(@PathVariable Long id) {

		hubService.deactivateHub(id);
		return ResponseEntity.noContent().build();
	}
}