package vn.uteexpress.dto;

import jakarta.validation.constraints.NotNull;

public record AdminChangeUserRoleRequest(@NotNull AdminCreateUserRequest.AssignableRole role) {
}
