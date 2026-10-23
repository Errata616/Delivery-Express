package vn.uteexpress.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Locale;

import org.springframework.stereotype.Service;

import vn.uteexpress.dto.BookingRequest;
import vn.uteexpress.entity.FeeZone;
import vn.uteexpress.entity.ServiceType;
import vn.uteexpress.entity.ShippingFeeRule;
import vn.uteexpress.repository.ShippingFeeRuleRepository;

@Service
public class ShippingFeeService {

	// =========================================================
	// DEFAULT PRICING (VND) - used only when no active rule exists in the database
	// =========================================================

	private static final BigDecimal BASE_SAME_PROVINCE = BigDecimal.valueOf(15000);

	// Added for every 0.5 kg after the first 0.5 kg (which the base fee includes)
	private static final BigDecimal STEP_SAME_PROVINCE = BigDecimal.valueOf(2500);

	private static final BigDecimal WEIGHT_STEP_KG = new BigDecimal("0.5");

	// Volumetric weight (kg) = length x width x height (cm) / 5000
	private static final BigDecimal VOLUMETRIC_DIVISOR = BigDecimal.valueOf(5000);

	// Express costs 60% more than standard (on base + weight fee)
	private static final BigDecimal EXPRESS_EXTRA_RATE = new BigDecimal("0.60");

	// COD fee = 1% of the COD amount, at least 5,000
	private static final BigDecimal COD_RATE = new BigDecimal("0.01");
	private static final BigDecimal COD_MIN_FEE = BigDecimal.valueOf(5000);

	// Every fee component is rounded UP to a multiple of this
	private static final BigDecimal ROUNDING_UNIT = BigDecimal.valueOf(500);

	// =========================================================
	// RESULT
	// =========================================================

	public record FeeQuote(boolean sameProvince, ServiceType serviceType, BigDecimal chargeableWeightKg,
			BigDecimal baseFee, BigDecimal weightFee, BigDecimal expressSurcharge, BigDecimal codFee,
			BigDecimal totalFee, int minDays, int maxDays) {
	}

	private final ShippingFeeRuleRepository shippingFeeRuleRepository;

	public ShippingFeeService(ShippingFeeRuleRepository shippingFeeRuleRepository) {
		this.shippingFeeRuleRepository = shippingFeeRuleRepository;
	}

	// =========================================================
	// CALCULATE FEE
	// =========================================================

	public FeeQuote calculate(BookingRequest request) {

		if (request == null) {
			throw new IllegalArgumentException("Thông tin đơn hàng không được null");
		}

		if (!isHoChiMinhCity(request.getPickupProvince())
				|| !isHoChiMinhCity(request.getDeliveryProvince())) {
			throw new IllegalArgumentException("UTEExpress hiện chỉ hỗ trợ gửi và nhận hàng trong TP. Hồ Chí Minh");
		}

		if (request.getWeightKg() == null || request.getWeightKg().signum() <= 0) {
			throw new IllegalArgumentException("Cân nặng không hợp lệ");
		}

		ServiceType serviceType = request.getServiceType() != null ? request.getServiceType() : ServiceType.STANDARD;

		boolean sameProvince = isSameProvince(request.getPickupProvince(), request.getDeliveryProvince());

		BigDecimal chargeableWeight = getChargeableWeight(request);

		// ---------- CHANGED: read prices from the database, fall back to the constants ----------

		ShippingFeeRule rule = shippingFeeRuleRepository
				.findFirstByZoneAndActiveTrueOrderByIdDesc(FeeZone.SAME_PROVINCE)
				.orElse(null);

		BigDecimal baseFee = rule != null ? rule.getBaseFee()
				: BASE_SAME_PROVINCE;

		BigDecimal stepFee = rule != null ? rule.getStepFee()
				: STEP_SAME_PROVINCE;

		// Number of 0.5 kg steps, rounded up. The first step is included in baseFee.
		BigDecimal steps = chargeableWeight.divide(WEIGHT_STEP_KG, 0, RoundingMode.CEILING);
		BigDecimal extraSteps = steps.subtract(BigDecimal.ONE).max(BigDecimal.ZERO);
		BigDecimal weightFee = stepFee.multiply(extraSteps);

		// ---------- end of CHANGED ----------

		BigDecimal expressSurcharge = BigDecimal.ZERO;

		if (serviceType == ServiceType.EXPRESS) {
			expressSurcharge = roundUp(baseFee.add(weightFee).multiply(EXPRESS_EXTRA_RATE));
		}

		BigDecimal codFee = calculateCodFee(request.getCodAmount());

		BigDecimal totalFee = baseFee.add(weightFee).add(expressSurcharge).add(codFee);

		int minDays;
		int maxDays;

		minDays = 1;
		maxDays = serviceType == ServiceType.EXPRESS ? 1 : 2;

		return new FeeQuote(sameProvince, serviceType, chargeableWeight, baseFee, weightFee, expressSurcharge, codFee,
				totalFee, minDays, maxDays);
	}

	// =========================================================
	// HELPERS
	// =========================================================

	// The larger of real weight and volumetric weight (if dimensions were given)
	private BigDecimal getChargeableWeight(BookingRequest request) {

		BigDecimal actual = request.getWeightKg();

		if (request.getLengthCm() == null || request.getWidthCm() == null || request.getHeightCm() == null) {
			return actual;
		}

		BigDecimal volume = BigDecimal
				.valueOf((long) request.getLengthCm() * request.getWidthCm() * request.getHeightCm());

		BigDecimal volumetric = volume.divide(VOLUMETRIC_DIVISOR, 2, RoundingMode.CEILING);

		return actual.max(volumetric);
	}

	private BigDecimal calculateCodFee(BigDecimal codAmount) {

		if (codAmount == null || codAmount.signum() <= 0) {
			return BigDecimal.ZERO;
		}

		return roundUp(codAmount.multiply(COD_RATE).max(COD_MIN_FEE));
	}

	private BigDecimal roundUp(BigDecimal value) {

		return value.divide(ROUNDING_UNIT, 0, RoundingMode.CEILING).multiply(ROUNDING_UNIT);
	}

	private boolean isSameProvince(String a, String b) {

		return normalizeProvince(a).equals(normalizeProvince(b));
	}

	private boolean isHoChiMinhCity(String province) {

		return "ho chi minh".equals(normalizeProvince(province));
	}

	// "TP. Hồ Chí Minh", "Thành phố Hồ Chí Minh" and "ho chi minh" all become "ho chi minh"
	public String normalizeProvince(String province) {

		if (province == null) {
			return "";
		}

		String s = Normalizer.normalize(province.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "").replace('đ', 'd');

		s = s.replaceAll("[.\\-]", " ").replaceAll("\\s+", " ").trim();

		s = s.replaceAll("^(thanh pho|tp|tinh)\\s+", "");
		String compact = s.replace(" ", "");
		if ("hcm".equals(compact) || "tphcm".equals(compact) || "hochiminh".equals(compact)
				|| "hochiminhcity".equals(compact) || "saigon".equals(compact)) {
			return "ho chi minh";
		}
		return s;
	}
}