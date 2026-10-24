package vn.uteexpress.dto;

import vn.uteexpress.entity.User;

public record UserProfileResponse(String username, String avatarUrl) {

	public static UserProfileResponse from(User user) {
		return new UserProfileResponse(user.getUsername(), user.getAvatarUrl());
	}
}
