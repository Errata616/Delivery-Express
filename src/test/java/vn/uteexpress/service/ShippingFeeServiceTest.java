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
	void calculatesDimensionalWeightExpressSurchargeAndCodFee() {
		ShippingFeeService service = new ShippingFeeService(mock(ShippingFeeRuleRepository.class));
		BookingRequest request = new BookingRequest();
		request.setPickupProvince("TP. Hồ Chí Minh");
		request.setDeliveryProvince("TP. Hồ Chí Minh");
		request.setWeightKg(new BigDecimal("0.5"));
		request.setLengthCm(50);
		request.setWidthCm(40);
		request.setHeightCm(30);
		request.setServiceType(vn.uteexpress.entity.ServiceType.EXPRESS);
		request.setCodAmount(new BigDecimal("100000"));

		ShippingFeeService.FeeQuote quote = service.calculate(request);

		assertEquals(0, new BigDecimal("12.00").compareTo(quote.chargeableWeightKg()));
		assertEquals(0, new BigDecimal("15000").compareTo(quote.baseFee()));
		assertEquals(0, new BigDecimal("57500").compareTo(quote.weightFee()));
		assertEquals(0, new BigDecimal("43500").compareTo(quote.expressSurcharge()));
		assertEquals(0, new BigDecimal("5000").compareTo(quote.codFee()));
		assertEquals(0, new BigDecimal("121000").compareTo(quote.totalFee()));
		assertEquals(1, quote.minDays());
		assertEquals(1, quote.maxDays());
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
