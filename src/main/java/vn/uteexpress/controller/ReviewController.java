package vn.uteexpress.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.dto.PageResponse;
import vn.uteexpress.dto.ReviewRequest;
import vn.uteexpress.dto.ReviewResponse;
import vn.uteexpress.security.CurrentUser;
import vn.uteexpress.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;
	private final CurrentUser currentUser;

	public ReviewController(ReviewService reviewService, CurrentUser currentUser) {
		this.reviewService = reviewService;
		this.currentUser = currentUser;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody ReviewRequest request) {

		ReviewResponse created = reviewService.create(currentUser.getUser().getId(), request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Cảm ơn bạn đã đánh giá", created));
	}

	@GetMapping("/shipment/{shipmentId}")
	public ResponseEntity<ApiResponse<ReviewResponse>> getByShipment(@PathVariable Long shipmentId) {

		return ResponseEntity.ok(ApiResponse.ok(reviewService.getByShipment(shipmentId, currentUser.getUser())));
	}

	@GetMapping("/shipper/{shipperId}")
	public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getShipperReviews(@PathVariable Long shipperId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

		return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(reviewService.getShipperReviews(shipperId,
				PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))))));
	}

	@GetMapping("/shipper/{shipperId}/summary")
	public ResponseEntity<ApiResponse<ReviewService.RatingSummary>> getShipperSummary(@PathVariable Long shipperId) {

		return ResponseEntity.ok(ApiResponse.ok(reviewService.getShipperSummary(shipperId)));
	}
}