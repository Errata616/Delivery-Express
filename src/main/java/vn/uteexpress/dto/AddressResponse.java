package vn.uteexpress.dto;

import vn.uteexpress.entity.Address;

public record AddressResponse(Long id, String receiverName, String phone, String address, String ward,
		String district, String city, boolean defaultAddress) {

	public static AddressResponse from(Address a) {
		return new AddressResponse(a.getId(), a.getReceiverName(), a.getPhone(), a.getAddress(), a.getWard(),
				a.getDistrict(), a.getCity(), a.isDefaultAddress());
	}
}