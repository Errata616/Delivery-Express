package vn.uteexpress.dto;

import vn.uteexpress.entity.User;

public record UserResponse(Long id, String username, String email, String fullName, String phone, String role,
		boolean enabled) {

	public static UserResponse from(User u) {
		return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getFullName(), u.getPhone(),
				u.getRole() != null ? u.getRole().getName() : null, u.isEnabled());
	}
}