package vn.uteexpress.dto;

import vn.uteexpress.entity.User;

public record AdminUserResponse(
		Long id,
		String username,
		String email,
		String fullName,
		String phone,
		String role,
		boolean enabled) {

	public static AdminUserResponse from(User user) {
		return new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
				user.getPhone(), user.getRole().getName(), user.isEnabled());
	}
}
