package vn.uteexpress.dto;

import vn.uteexpress.entity.Hub;

public record HubResponse(Long id, String code, String name, String address, String district, String province,
		String phone, boolean active) {

	public static HubResponse from(Hub h) {
		return new HubResponse(h.getId(), h.getCode(), h.getName(), h.getAddress(), h.getDistrict(),
				h.getProvince(), h.getPhone(), h.isActive());
	}
}