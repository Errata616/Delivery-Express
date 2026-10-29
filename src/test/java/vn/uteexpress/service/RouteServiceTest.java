package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class RouteServiceTest {

	@Test
	void reportsUnrecognizedDistrictInVietnamese() {
		RouteService service = new RouteService();

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.plan("Địa chỉ không tồn tại", "Quận 1"));

		assertEquals("Không nhận diện được quận/huyện: Địa chỉ không tồn tại", exception.getReason());
	}
}
