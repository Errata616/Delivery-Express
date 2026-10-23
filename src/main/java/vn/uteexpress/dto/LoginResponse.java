package vn.uteexpress.dto;

public record LoginResponse(String token, String refreshToken, String username, String role) {
}