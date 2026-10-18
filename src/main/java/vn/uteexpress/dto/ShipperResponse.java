package vn.uteexpress.dto;

import java.util.List;

import vn.uteexpress.entity.User;

public record ShipperResponse(Long id, String fullName, String phone) {

	public static ShipperResponse from(User user) {
		return new ShipperResponse(user.getId(), user.getFullName(), user.getPhone());
	}

	public static List<ShipperResponse> fromUsers(List<User> users) {
		return users.stream().map(ShipperResponse::from).toList();
	}
}