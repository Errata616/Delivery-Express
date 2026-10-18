package vn.uteexpress.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.dto.RegisterRequest;
import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.RoleRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void register(RegisterRequest request) {
		String username = request.getUsername().trim();
		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		if (userRepository.existsByUsername(username)) {
			throw new IllegalArgumentException("Username is already in use");
		}
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("Email is already in use");
		}
		if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new IllegalArgumentException("Password must not exceed 72 UTF-8 bytes");
		}

		Role userRole = roleRepository.findByName("USER")
				.orElseThrow(() -> new IllegalStateException("Default USER role is not configured"));

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setFullName(request.getFullName() == null ? null : request.getFullName().trim());
		user.setPhone(request.getPhone() == null ? null : request.getPhone().trim());
		user.setEnabled(true);
		user.setRole(userRole);

		userRepository.save(user);
	}
}
