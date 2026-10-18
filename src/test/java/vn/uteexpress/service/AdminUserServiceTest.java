package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import vn.uteexpress.dto.AdminChangeUserRoleRequest;
import vn.uteexpress.dto.AdminCreateUserRequest;
import vn.uteexpress.dto.AdminUserResponse;
import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.RoleRepository;
import vn.uteexpress.repository.RefreshTokenRepository;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.security.CurrentUser;

class AdminUserServiceTest {

	@Test
	void createsEnabledShipperWithEncodedPasswordAndSafeResponse() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		Role shipperRole = new Role("SHIPPER");

		when(userRepository.existsByUsername("newshipper")).thenReturn(false);
		when(userRepository.existsByEmail("shipper@example.com")).thenReturn(false);
		when(roleRepository.findByName("SHIPPER")).thenReturn(java.util.Optional.of(shipperRole));
		when(passwordEncoder.encode("StrongPass123")).thenReturn("encoded-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AdminUserService service = new AdminUserService(userRepository, roleRepository, passwordEncoder,
				refreshTokenRepository, currentUser);
		AdminUserResponse response = service.createUser(new AdminCreateUserRequest(" newshipper ",
				" Shipper@Example.com ", "StrongPass123", " New Shipper ", " 0900000001 ",
				AdminCreateUserRequest.AssignableRole.SHIPPER));

		assertEquals("newshipper", response.username());
		assertEquals("shipper@example.com", response.email());
		assertEquals("New Shipper", response.fullName());
		assertEquals("0900000001", response.phone());
		assertEquals("SHIPPER", response.role());
		assertTrue(response.enabled());
		verify(passwordEncoder).encode("StrongPass123");
		verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
				"encoded-password".equals(user.getPassword()) && user.isEnabled()
						&& user.getRole() == shipperRole));
	}

	@Test
	void rejectsDuplicateUsername() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		when(userRepository.existsByUsername("existing")).thenReturn(true);

		AdminUserService service = new AdminUserService(userRepository, roleRepository, passwordEncoder,
				refreshTokenRepository, currentUser);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.createUser(new AdminCreateUserRequest("existing", "user@example.com", "StrongPass123",
						"Existing User", null, AdminCreateUserRequest.AssignableRole.MANAGER)));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void changesStaffRoleAndRevokesExistingRefreshTokens() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		User user = mock(User.class);
		Role previousRole = new Role("SHIPPER");
		Role managerRole = new Role("MANAGER");

		when(userRepository.findById(20L)).thenReturn(Optional.of(user));
		when(user.getId()).thenReturn(20L);
		when(user.getRole()).thenReturn(previousRole, previousRole, managerRole);
		when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole));
		when(userRepository.save(user)).thenReturn(user);
		when(user.getUsername()).thenReturn("staff");
		when(user.getEmail()).thenReturn("staff@example.com");
		when(user.getFullName()).thenReturn("Staff Member");
		when(user.getPhone()).thenReturn("0900000000");
		when(user.isEnabled()).thenReturn(true);

		AdminUserService service = new AdminUserService(userRepository, roleRepository, passwordEncoder,
				refreshTokenRepository, currentUser);
		AdminUserResponse response = service.changeRole(20L,
				new AdminChangeUserRoleRequest(AdminCreateUserRequest.AssignableRole.MANAGER));

		assertEquals("MANAGER", response.role());
		verify(user).setRole(managerRole);
		verify(refreshTokenRepository).revokeAllByUserId(20L);
	}

	@Test
	void listsSafeUserDtosInsteadOfReturningUserEntities() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		User user = mock(User.class);
		when(userRepository.findAll()).thenReturn(List.of(user));
		when(user.getRole()).thenReturn(new Role("SHIPPER"));
		when(user.getUsername()).thenReturn("shipper");
		when(user.getEmail()).thenReturn("shipper@example.com");
		when(user.isEnabled()).thenReturn(true);

		AdminUserService service = new AdminUserService(userRepository, roleRepository, passwordEncoder,
				refreshTokenRepository, currentUser);

		assertEquals("SHIPPER", service.getUsers().getFirst().role());
		assertEquals(7, AdminUserResponse.class.getRecordComponents().length);
	}
}
