package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.uteexpress.dto.AdminChangeUserRoleRequest;
import vn.uteexpress.dto.AdminCreateUserRequest;
import vn.uteexpress.dto.AdminSetUserEnabledRequest;
import vn.uteexpress.dto.AdminUserResponse;
import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

	private final AdminUserService adminUserService;

	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getUsers() {
		return ResponseEntity.ok(ApiResponse.ok(adminUserService.getUsers()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok(adminUserService.getUser(id)));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(
			@Valid @RequestBody AdminCreateUserRequest request) {
		AdminUserResponse created = adminUserService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created", created));
	}

	@PutMapping("/{id}/role")
	public ResponseEntity<ApiResponse<AdminUserResponse>> changeRole(@PathVariable Long id,
			@Valid @RequestBody AdminChangeUserRoleRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(adminUserService.changeRole(id, request)));
	}

	@PatchMapping("/{id}/enabled")
	public ResponseEntity<ApiResponse<AdminUserResponse>> setEnabled(@PathVariable Long id,
			@Valid @RequestBody AdminSetUserEnabledRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(adminUserService.setEnabled(id, request)));
	}
}
