package vn.uteexpress.dto;

import java.util.List;

// match: EXACT | NEARBY | NO_ZONE | UNKNOWN (district not recognised, e.g. outside HCM)
public record ShipperSuggestion(Long id, String fullName, String phone, String match, Integer hops,
		List<String> zones) {
}