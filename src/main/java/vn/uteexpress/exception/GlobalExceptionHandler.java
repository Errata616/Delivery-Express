package vn.uteexpress.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import vn.uteexpress.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {

		Map<String, String> fieldErrors = new LinkedHashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

		return ResponseEntity.badRequest().body(ApiResponse.error("Dữ liệu không hợp lệ", fieldErrors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
		return fail(HttpStatus.BAD_REQUEST, "Nội dung request không hợp lệ (sai định dạng JSON hoặc giá trị không đúng)");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return fail(HttpStatus.BAD_REQUEST, "Giá trị của tham số '" + ex.getName() + "' không hợp lệ");
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
		return fail(HttpStatus.BAD_REQUEST, "Thiếu tham số: " + ex.getParameterName());
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
		return fail(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
		return fail(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConflict(DataIntegrityViolationException ex) {

		log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());

		return fail(HttpStatus.CONFLICT, "Dữ liệu bị trùng hoặc vi phạm ràng buộc");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
		return fail(statusFor(ex.getMessage()), ex.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException ex) {

		if (ex instanceof ErrorResponse errorResponse) {

			HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
			String detail = errorResponse.getBody().getDetail();

			return fail(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR,
					detail != null ? detail : ex.getMessage());
		}

		// A plain RuntimeException is a business error thrown on purpose
		if (ex.getClass() == RuntimeException.class) {
			return fail(statusFor(ex.getMessage()), ex.getMessage());
		}

		// Anything else is a bug: log it, hide the details
		log.error("Unhandled exception", ex);

		return fail(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");
	}

	private HttpStatus statusFor(String message) {

		if (message != null) {
			if (message.startsWith("Không tìm thấy")) {
				return HttpStatus.NOT_FOUND;
			}
			if (message.startsWith("Không có quyền")) {
				return HttpStatus.FORBIDDEN;
			}
		}

		return HttpStatus.BAD_REQUEST;
	}

	private ResponseEntity<ApiResponse<Void>> fail(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(ApiResponse.error(message));
	}
}