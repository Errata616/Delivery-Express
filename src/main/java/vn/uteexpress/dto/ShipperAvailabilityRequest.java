package vn.uteexpress.dto;

import jakarta.validation.constraints.NotNull;

public record ShipperAvailabilityRequest(@NotNull Boolean available) {
}
