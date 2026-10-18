package vn.uteexpress.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import vn.uteexpress.dto.AdminCreateUserRequest;
import vn.uteexpress.dto.AdminChangeUserRoleRequest;
import vn.uteexpress.dto.AdminSetUserEnabledRequest;
import vn.uteexpress.dto.AdminUserResponse;
import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.RefreshTokenRepository;
import vn.uteexpress.repository.RoleRepository;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.security.CurrentUser;

@Service
public class AdminUserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenRepository refreshTokenRepository;
	private final CurrentUser currentUser;

	public AdminUserService(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, CurrentUser currentUser) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.refreshTokenRepository = refreshTokenRepository;
		this.currentUser = currentUser;
	}

	@Transactional(readOnly = true)
	public List<AdminUserResponse> getUsers() {
		return userRepository.findAll().stream().map(AdminUserResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public AdminUserResponse getUser(Long id) {
		return AdminUserResponse.from(findUser(id));
	}

	@Transactional
	public AdminUserResponse createUser(AdminCreateUserRequest request) {
		String username = request.username().trim();
		String email = request.email().trim().toLowerCase(Locale.ROOT);

		if (userRepository.existsByUsername(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
		}
		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
		}
		if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new IllegalArgumentException("Password must not exceed 72 UTF-8 bytes");
		}

		String roleName = request.role().name();
		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalStateException(roleName + " role is not configured"));

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setFullName(request.fullName().trim());
		user.setPhone(request.phone() == null ? null : request.phone().trim());
		user.setEnabled(true);
		user.setRole(role);

		return AdminUserResponse.from(userRepository.save(user));
	}

	@Transactional
	public AdminUserResponse changeRole(Long id, AdminChangeUserRoleRequest request) {
		User user = findUser(id);
		if ("ADMIN".equalsIgnoreCase(user.getRole().getName())) {
			throw new IllegalArgumentException("Administrator roles cannot be changed through this endpoint");
		}

		String roleName = request.role().name();
		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalStateException(roleName + " role is not configured"));
		if (!roleName.equalsIgnoreCase(user.getRole().getName())) {
			user.setRole(role);
			refreshTokenRepository.revokeAllByUserId(user.getId());
		}

		return AdminUserResponse.from(userRepository.save(user));
	}

	@Transactional
	public AdminUserResponse setEnabled(Long id, AdminSetUserEnabledRequest request) {
		User user = findUser(id);
		boolean enabled = request.enabled();

		if (!enabled && user.getId().equals(currentUser.getUser().getId())) {
			throw new IllegalArgumentException("You cannot disable your own account");
		}
		if (!enabled && "ADMIN".equalsIgnoreCase(user.getRole().getName())) {
			throw new IllegalArgumentException("Administrator accounts cannot be disabled through this endpoint");
		}

		if (user.isEnabled() != enabled) {
			user.setEnabled(enabled);
			if (!enabled) {
				refreshTokenRepository.revokeAllByUserId(user.getId());
			}
		}

		return AdminUserResponse.from(userRepository.save(user));
	}

	private User findUser(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("User id is required");
		}
		return userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}
}
