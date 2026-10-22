package vn.uteexpress.dto;

import java.util.List;

public record ShipperZoneResponse(Long shipperId, String fullName, String phone, boolean enabled, boolean available,
		List<String> districtCodes) {
}