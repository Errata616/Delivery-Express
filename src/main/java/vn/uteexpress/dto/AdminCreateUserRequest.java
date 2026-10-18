package vn.uteexpress.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
		@NotBlank @Size(min = 3, max = 50) String username,
		@NotBlank @Email @Size(max = 100) String email,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank @Size(max = 100) String fullName,
		@Size(max = 20) String phone,
		@NotNull AssignableRole role) {

	public enum AssignableRole {
		SHIPPER,
		MANAGER
	}
}
