package vn.uteexpress.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import vn.uteexpress.dto.ReviewRequest;
import vn.uteexpress.dto.ReviewResponse;
import vn.uteexpress.entity.Review;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class ReviewService {

	public record RatingSummary(double average, long count) {
	}

	private final ReviewRepository reviewRepository;
	private final ShipmentRepository shipmentRepository;
	private final UserRepository userRepository;

	public ReviewService(ReviewRepository reviewRepository, ShipmentRepository shipmentRepository,
			UserRepository userRepository) {
		this.reviewRepository = reviewRepository;
		this.shipmentRepository = shipmentRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public ReviewResponse create(Long customerId, ReviewRequest request) {

		Shipment shipment = findShipment(request.shipmentId());

		if (!isOwner(shipment, customerId)) {
			throw new IllegalArgumentException("Không có quyền đánh giá vận đơn này");
		}

		if (shipment.getStatus() != ShipmentStatus.DELIVERED) {
			throw new IllegalArgumentException("Chỉ được đánh giá sau khi đơn đã giao thành công");
		}

		if (shipment.getShipper() == null) {
			throw new IllegalArgumentException("Vận đơn chưa được gán shipper để đánh giá");
		}

		if (reviewRepository.existsByShipmentId(shipment.getId())) {
			throw new IllegalArgumentException("Vận đơn này đã được đánh giá");
		}

		User customer = userRepository.findById(customerId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		String comment = request.comment() == null || request.comment().isBlank() ? null : request.comment().trim();

		Review review = new Review(shipment, customer, shipment.getShipper(), request.rating(), comment);

		return ReviewResponse.from(reviewRepository.save(review));
	}

	@Transactional(readOnly = true)
	public ReviewResponse getByShipment(Long shipmentId, User viewer) {

		Shipment shipment = findShipment(shipmentId);

		String role = viewer.getRole() != null ? viewer.getRole().getName() : "";
		boolean privileged = role.equals("ADMIN") || role.equals("MANAGER");
		boolean isShipper = shipment.getShipper() != null && shipment.getShipper().getId().equals(viewer.getId());

		if (!privileged && !isShipper && !isOwner(shipment, viewer.getId())) {
			throw new IllegalArgumentException("Không có quyền xem đánh giá này");
		}

		Review review = reviewRepository.findByShipmentId(shipmentId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá cho vận đơn này"));

		return ReviewResponse.from(review);
	}

	@Transactional(readOnly = true)
	public Page<ReviewResponse> getShipperReviews(Long shipperId, Pageable pageable) {
		return reviewRepository.findByShipperIdOrderByCreatedAtDesc(shipperId, pageable).map(ReviewResponse::from);
	}

	@Transactional(readOnly = true)
	public List<ReviewResponse> getCustomerReviews(Long customerId) {
		return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
				.map(ReviewResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public RatingSummary getShipperSummary(Long shipperId) {

		Double average = reviewRepository.averageRatingByShipper(shipperId);

		return new RatingSummary(average == null ? 0.0 : Math.round(average * 10) / 10.0,
				reviewRepository.countByShipperId(shipperId));
	}

	private Shipment findShipment(Long id) {
		return shipmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy vận đơn"));
	}

	private boolean isOwner(Shipment shipment, Long userId) {

		if (shipment.getCustomer() != null && shipment.getCustomer().getId().equals(userId)) {
			return true;
		}

		return shipment.getOrder() != null && shipment.getOrder().getUser() != null
				&& shipment.getOrder().getUser().getId().equals(userId);
	}
}