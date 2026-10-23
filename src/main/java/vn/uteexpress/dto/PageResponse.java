package vn.uteexpress.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages());
	}

	// Convert entities to DTOs while wrapping: PageResponse.from(page, ShipmentResponse::from)
	public static <T, R> PageResponse<R> from(Page<T> page, Function<? super T, ? extends R> mapper) {
		return from(page.map(mapper));
	}
}