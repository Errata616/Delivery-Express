package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import vn.uteexpress.repository.AddressRepository;
import vn.uteexpress.repository.HiddenShipmentRepository;
import vn.uteexpress.repository.NotificationRepository;
import vn.uteexpress.repository.OrderRepository;
import vn.uteexpress.repository.RefreshTokenRepository;
import vn.uteexpress.repository.ReviewRepository;
import vn.uteexpress.repository.RoleRepository;
import vn.uteexpress.repository.ShipmentEventRepository;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.ShipperZoneRepository;
import vn.uteexpress.repository.ShopRepository;
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

		AdminUserService service = service(userRepository, roleRepository, passwordEncoder,
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

		AdminUserService service = service(userRepository, roleRepository, passwordEncoder,
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

		AdminUserService service = service(userRepository, roleRepository, passwordEncoder,
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

		AdminUserService service = service(userRepository, roleRepository, passwordEncoder,
				refreshTokenRepository, currentUser);

		assertEquals("SHIPPER", service.getUsers().getFirst().role());
		assertEquals(10, AdminUserResponse.class.getRecordComponents().length);
	}

	@Test
	void assignsShipperToManager() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		User shipper = new User("shipper", "shipper@example.com", "hash", "Shipper", null, true,
				new Role("SHIPPER"));
		User manager = new User("manager", "manager@example.com", "hash", "Manager", null, true,
				new Role("MANAGER"));
		when(userRepository.findById(10L)).thenReturn(Optional.of(shipper));
		when(userRepository.findById(20L)).thenReturn(Optional.of(manager));
		when(userRepository.save(shipper)).thenReturn(shipper);

		AdminUserService service = service(userRepository, roleRepository, passwordEncoder,
				refreshTokenRepository, currentUser);
		AdminUserResponse response = service.assignShipperToManager(10L, 20L);

		assertSame(manager, shipper.getManager());
		assertEquals("Manager", response.managerName());
	}

	@Test
	void rejectsAssigningShipperToNonManager() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		User shipper = new User("shipper", "shipper@example.com", "hash", "Shipper", null, true,
				new Role("SHIPPER"));
		User anotherShipper = new User("another", "another@example.com", "hash", "Another", null, true,
				new Role("SHIPPER"));
		when(userRepository.findById(10L)).thenReturn(Optional.of(shipper));
		when(userRepository.findById(20L)).thenReturn(Optional.of(anotherShipper));

		AdminUserService service = service(userRepository, roleRepository, passwordEncoder,
				refreshTokenRepository, currentUser);

		assertThrows(IllegalArgumentException.class, () -> service.assignShipperToManager(10L, 20L));
		assertNull(shipper.getManager());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void deletesManagerAndRemovesItsAccountReferences() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		User manager = mock(User.class);
		User admin = mock(User.class);
		User shipper = mock(User.class);
		AddressRepository addressRepository = mock(AddressRepository.class);
		HiddenShipmentRepository hiddenShipmentRepository = mock(HiddenShipmentRepository.class);
		NotificationRepository notificationRepository = mock(NotificationRepository.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		ReviewRepository reviewRepository = mock(ReviewRepository.class);
		ShipmentEventRepository shipmentEventRepository = mock(ShipmentEventRepository.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		ShipperZoneRepository shipperZoneRepository = mock(ShipperZoneRepository.class);
		ShopRepository shopRepository = mock(ShopRepository.class);

		when(userRepository.findById(20L)).thenReturn(Optional.of(manager));
		when(manager.getId()).thenReturn(20L);
		when(manager.getRole()).thenReturn(new Role("MANAGER"));
		when(userRepository.findByManager_Id(20L)).thenReturn(List.of(shipper));
		when(currentUser.getUser()).thenReturn(admin);
		when(admin.getId()).thenReturn(1L);

		AdminUserService service = new AdminUserService(userRepository, roleRepository, mock(PasswordEncoder.class),
				refreshTokenRepository, currentUser, addressRepository, hiddenShipmentRepository,
				notificationRepository, orderRepository, reviewRepository, shipmentEventRepository,
				shipmentRepository, shipperZoneRepository, shopRepository);
		service.deleteUser(20L);

		verify(shipper).setManager(null);
		verify(userRepository).saveAll(List.of(shipper));
		verify(shipmentEventRepository).clearUpdatedBy(20L);
		verify(hiddenShipmentRepository).deleteByUserId(20L);
		verify(notificationRepository).deleteAllByUserId(20L);
		verify(refreshTokenRepository).deleteAllByUserId(20L);
		verify(userRepository).delete(manager);
	}

	@Test
	void refusesDeletingAccountWithHistoricalOrders() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		User account = mock(User.class);
		User admin = mock(User.class);
		OrderRepository orderRepository = mock(OrderRepository.class);
		when(userRepository.findById(20L)).thenReturn(Optional.of(account));
		when(account.getId()).thenReturn(20L);
		when(account.getRole()).thenReturn(new Role("USER"));
		when(currentUser.getUser()).thenReturn(admin);
		when(admin.getId()).thenReturn(1L);
		when(orderRepository.countByUserId(20L)).thenReturn(1L);

		AdminUserService service = new AdminUserService(userRepository, roleRepository, mock(PasswordEncoder.class),
				refreshTokenRepository, currentUser, mock(AddressRepository.class),
				mock(HiddenShipmentRepository.class), mock(NotificationRepository.class), orderRepository,
				mock(ReviewRepository.class), mock(ShipmentEventRepository.class), mock(ShipmentRepository.class),
				mock(ShipperZoneRepository.class), mock(ShopRepository.class));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.deleteUser(20L));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		verify(userRepository, never()).delete(account);
	}

	@Test
	void refusesDeletingShipperWithActiveShipments() {
		UserRepository userRepository = mock(UserRepository.class);
		RoleRepository roleRepository = mock(RoleRepository.class);
		RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
		CurrentUser currentUser = mock(CurrentUser.class);
		User shipper = mock(User.class);
		User admin = mock(User.class);
		ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
		when(userRepository.findById(20L)).thenReturn(Optional.of(shipper));
		when(shipper.getId()).thenReturn(20L);
		when(shipper.getRole()).thenReturn(new Role("SHIPPER"));
		when(currentUser.getUser()).thenReturn(admin);
		when(admin.getId()).thenReturn(1L);
		when(shipmentRepository.countByShipperIdAndStatusIn(any(), any())).thenReturn(1L);

		AdminUserService service = new AdminUserService(userRepository, roleRepository, mock(PasswordEncoder.class),
				refreshTokenRepository, currentUser, mock(AddressRepository.class),
				mock(HiddenShipmentRepository.class), mock(NotificationRepository.class),
				mock(OrderRepository.class), mock(ReviewRepository.class),
				mock(ShipmentEventRepository.class), shipmentRepository,
				mock(ShipperZoneRepository.class), mock(ShopRepository.class));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.deleteUser(20L));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		verify(userRepository, never()).delete(shipper);
	}

	private static AdminUserService service(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository, CurrentUser currentUser) {
		return new AdminUserService(userRepository, roleRepository, passwordEncoder, refreshTokenRepository, currentUser,
				mock(AddressRepository.class), mock(HiddenShipmentRepository.class),
				mock(NotificationRepository.class), mock(OrderRepository.class), mock(ReviewRepository.class),
				mock(ShipmentEventRepository.class), mock(ShipmentRepository.class),
				mock(ShipperZoneRepository.class), mock(ShopRepository.class));
	}
}
