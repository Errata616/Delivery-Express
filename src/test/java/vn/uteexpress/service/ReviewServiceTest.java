package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import vn.uteexpress.dto.ReviewRequest;
import vn.uteexpress.dto.ReviewResponse;
import vn.uteexpress.entity.Review;
import vn.uteexpress.entity.Shipment;
import vn.uteexpress.entity.ShipmentStatus;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

class ReviewServiceTest {

	@Test
	void customerCanReviewDeliveredShipmentOnce() {
		ReviewRepository reviewRepository = mock(ReviewRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		Shipment shipment = deliveredShipment(15L, 4L, 9L);
		User customer = mock(User.class);
		User shipper = mock(User.class);
		when(shipment.getCustomer()).thenReturn(customer);
		when(shipment.getShipper()).thenReturn(shipper);
		when(customer.getId()).thenReturn(4L);
		when(shipment.getTrackingCode()).thenReturn("UTE-15");
		when(customer.getFullName()).thenReturn("Customer Name");
		when(shipper.getId()).thenReturn(9L);
		when(userRepository.findById(4L)).thenReturn(Optional.of(customer));
		when(shipmentRepository.findById(15L)).thenReturn(Optional.of(shipment));
		when(reviewRepository.existsByShipmentId(15L)).thenReturn(false);
		when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ReviewService service = new ReviewService(reviewRepository, shipmentRepository, userRepository);
		ReviewResponse response = service.create(4L, new ReviewRequest(15L, 5, "  Giao hàng rất tốt  "));

		assertEquals(15L, response.shipmentId());
		assertEquals(9L, response.shipperId());
		assertEquals(5, response.rating());
		ArgumentCaptor<Review> savedReview = ArgumentCaptor.forClass(Review.class);
		verify(reviewRepository).save(savedReview.capture());
		assertEquals("Giao hàng rất tốt", savedReview.getValue().getComment());
	}

	@Test
	void onlyShipmentOwnerCanReview() {
		ReviewRepository reviewRepository = mock(ReviewRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		Shipment shipment = deliveredShipment(15L, 4L, 9L);
		User customer = mock(User.class);
		when(shipment.getCustomer()).thenReturn(customer);
		when(customer.getId()).thenReturn(4L);
		when(shipmentRepository.findById(15L)).thenReturn(Optional.of(shipment));
		ReviewService service = new ReviewService(reviewRepository, shipmentRepository, mock(UserRepository.class));

		assertThrows(IllegalArgumentException.class,
				() -> service.create(7L, new ReviewRequest(15L, 5, null)));
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	void onlyDeliveredShipmentWithAssignedShipperCanBeReviewed() {
		ReviewRepository reviewRepository = mock(ReviewRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		UserRepository userRepository = mock(UserRepository.class);
		Shipment shipment = mock(Shipment.class);
		User customer = mock(User.class);
		when(shipment.getId()).thenReturn(15L);
		when(shipment.getCustomer()).thenReturn(customer);
		when(customer.getId()).thenReturn(4L);
		when(shipmentRepository.findById(15L)).thenReturn(Optional.of(shipment));
		ReviewService service = new ReviewService(reviewRepository, shipmentRepository, userRepository);

		assertThrows(IllegalArgumentException.class,
				() -> service.create(4L, new ReviewRequest(15L, 5, null)));
		when(shipment.getStatus()).thenReturn(ShipmentStatus.DELIVERED);
		assertThrows(IllegalArgumentException.class,
				() -> service.create(4L, new ReviewRequest(15L, 5, null)));
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	void shipmentCannotBeReviewedTwice() {
		ReviewRepository reviewRepository = mock(ReviewRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		Shipment shipment = deliveredShipment(15L, 4L, 9L);
		User customer = mock(User.class);
		User shipper = mock(User.class);
		when(shipment.getCustomer()).thenReturn(customer);
		when(shipment.getShipper()).thenReturn(shipper);
		when(customer.getId()).thenReturn(4L);
		when(shipmentRepository.findById(15L)).thenReturn(Optional.of(shipment));
		when(reviewRepository.existsByShipmentId(15L)).thenReturn(true);
		ReviewService service = new ReviewService(reviewRepository, shipmentRepository, mock(UserRepository.class));

		assertThrows(IllegalArgumentException.class,
				() -> service.create(4L, new ReviewRequest(15L, 5, null)));
		verify(reviewRepository, never()).save(any(Review.class));
	}

	private Shipment deliveredShipment(Long shipmentId, Long customerId, Long shipperId) {
		Shipment shipment = mock(Shipment.class);
		User customer = mock(User.class);
		User shipper = mock(User.class);
		when(shipment.getId()).thenReturn(shipmentId);
		when(shipment.getStatus()).thenReturn(ShipmentStatus.DELIVERED);
		when(shipment.getCustomer()).thenReturn(customer);
		when(shipment.getShipper()).thenReturn(shipper);
		when(customer.getId()).thenReturn(customerId);
		when(shipper.getId()).thenReturn(shipperId);
		return shipment;
	}
}
