package vn.uteexpress.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.dto.LoginRequest;
import vn.uteexpress.dto.LoginResponse;
import vn.uteexpress.dto.RefreshTokenRequest;
import vn.uteexpress.dto.RegisterRequest;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.security.JwtTokenService;
import vn.uteexpress.service.AuthService;
import vn.uteexpress.service.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenService jwtTokenService;
	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;
	private final UserDetailsService userDetailsService;

	public ApiAuthController(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService,
			AuthService authService, RefreshTokenService refreshTokenService, UserRepository userRepository,
			UserDetailsService userDetailsService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenService = jwtTokenService;
		this.authService = authService;
		this.refreshTokenService = refreshTokenService;
		this.userRepository = userRepository;
		this.userDetailsService = userDetailsService;
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {

		if (request.getUsername() == null || request.getUsername().isBlank() || request.getPassword() == null
				|| request.getPassword().isBlank()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("Username and password are required"));
		}

		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();

			User user = userRepository.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

			LoginResponse response = new LoginResponse(jwtTokenService.generateToken(userDetails),
					refreshTokenService.create(user), userDetails.getUsername(), extractRole(userDetails));

			return ResponseEntity.ok(ApiResponse.ok(response));

		} catch (AuthenticationException exception) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error("Invalid username or password"));
		}
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {

		try {
			authService.register(request);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.<Void>ok("Account created. You can now log in.", null));

		} catch (IllegalArgumentException exception) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(exception.getMessage()));
		}
	}

	// Trade a valid refresh token for a new access token AND a new refresh token
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {

		RefreshTokenService.Rotated rotated = refreshTokenService.rotate(request.refreshToken());

		UserDetails userDetails = userDetailsService.loadUserByUsername(rotated.username());

		LoginResponse response = new LoginResponse(jwtTokenService.generateToken(userDetails),
				rotated.newRawToken(), userDetails.getUsername(), extractRole(userDetails));

		return ResponseEntity.ok(ApiResponse.ok(response));
	}

	// Ends the session. The access token stays valid until it expires (keep it short, e.g. 15 min).
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {

		refreshTokenService.revoke(request.refreshToken());

		return ResponseEntity.ok(ApiResponse.<Void>ok("Đã đăng xuất", null));
	}

	private String extractRole(UserDetails userDetails) {

		String role = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElse("");

		return role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
	}
}