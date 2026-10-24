package vn.uteexpress.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.dto.UserProfileResponse;
import vn.uteexpress.security.CurrentUser;
import vn.uteexpress.service.UserProfileService;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

	private final CurrentUser currentUser;
	private final UserProfileService userProfileService;

	public UserProfileController(CurrentUser currentUser, UserProfileService userProfileService) {
		this.currentUser = currentUser;
		this.userProfileService = userProfileService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
		return ResponseEntity.ok(ApiResponse.ok(UserProfileResponse.from(currentUser.getUser())));
	}

	@PostMapping("/avatar")
	public ResponseEntity<ApiResponse<UserProfileResponse>> updateAvatar(@RequestParam("file") MultipartFile file) {
		UserProfileResponse profile = userProfileService.updateAvatar(currentUser.getUser(), file);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("Đã cập nhật ảnh đại diện", profile));
	}

	@DeleteMapping("/avatar")
	public ResponseEntity<ApiResponse<UserProfileResponse>> removeAvatar() {
		UserProfileResponse profile = userProfileService.removeAvatar(currentUser.getUser());
		return ResponseEntity.ok(ApiResponse.ok("Đã gỡ ảnh đại diện", profile));
	}
}
