package vn.uteexpress.dto;

import vn.uteexpress.entity.User;

public record AdminUserResponse(
		Long id,
		String username,
		String email,
		String fullName,
		String phone,
		String role,
		boolean enabled,
		boolean available,
		Long managerId,
		String managerName) {

	public static AdminUserResponse from(User user) {
		User manager = user.getManager();
		return new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
				user.getPhone(), user.getRole().getName(), user.isEnabled(), user.isAvailable(),
				manager == null ? null : manager.getId(),
				manager == null ? null : manager.getFullName() == null ? manager.getUsername() : manager.getFullName());
	}
}
