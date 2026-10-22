package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import vn.uteexpress.dto.BookingRequest;
import vn.uteexpress.entity.FeeZone;
import vn.uteexpress.repository.ShippingFeeRuleRepository;

class ShippingFeeServiceTest {

	@Test
	void acceptsHoChiMinhAliasesAndUsesLocalFeeZone() {
		ShippingFeeRuleRepository rules = mock(ShippingFeeRuleRepository.class);
		ShippingFeeService service = new ShippingFeeService(rules);
		BookingRequest request = new BookingRequest();
		request.setPickupProvince("TP. Hồ Chí Minh");
		request.setDeliveryProvince("Sài Gòn");
		request.setWeightKg(new BigDecimal("0.5"));

		ShippingFeeService.FeeQuote quote = service.calculate(request);

		assertTrue(quote.sameProvince());
		assertEquals(1, quote.minDays());
		assertEquals(2, quote.maxDays());
		verify(rules).findFirstByZoneAndActiveTrueOrderByIdDesc(FeeZone.SAME_PROVINCE);
		verifyNoMoreInteractions(rules);
	}

	@Test
	void rejectsBookingOutsideHoChiMinhCity() {
		ShippingFeeRuleRepository rules = mock(ShippingFeeRuleRepository.class);
		ShippingFeeService service = new ShippingFeeService(rules);
		BookingRequest request = new BookingRequest();
		request.setPickupProvince("TP. Hồ Chí Minh");
		request.setDeliveryProvince("Hà Nội");
		request.setWeightKg(new BigDecimal("0.5"));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> service.calculate(request));

		assertTrue(exception.getMessage().contains("chỉ hỗ trợ"));
		verifyNoInteractions(rules);
	}

	@Test
	void recognizesHoChiMinhCityNameVariants() {
		ShippingFeeService service = new ShippingFeeService(mock(ShippingFeeRuleRepository.class));

		assertEquals("ho chi minh", service.normalizeProvince("Thành phố Hồ Chí Minh"));
		assertEquals("ho chi minh", service.normalizeProvince("TPHCM"));
		assertEquals("ho chi minh", service.normalizeProvince("Ho Chi Minh City"));
	}
}
