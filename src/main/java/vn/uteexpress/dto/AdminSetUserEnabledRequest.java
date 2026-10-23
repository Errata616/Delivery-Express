package vn.uteexpress.dto;

import jakarta.validation.constraints.NotNull;

public record AdminSetUserEnabledRequest(@NotNull Boolean enabled) {
}
