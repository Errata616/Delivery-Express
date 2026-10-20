package vn.uteexpress.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.AdminShipmentService;

class AdminShipmentControllerTest {

	@Test
	void permitsAdminToDeleteCompletedShipment() {
		AdminShipmentService service = mock(AdminShipmentService.class);
		UserRepository users = mock(UserRepository.class);
		User admin = user("admin", "ADMIN");
		when(users.findByUsername("admin")).thenReturn(Optional.of(admin));
		AdminShipmentController controller = new AdminShipmentController(service, users);

		controller.deleteCompletedShipment(3L, auth("admin"));

		verify(service).deleteCompletedShipment(3L);
	}

	@Test
	void deniesManagerPermanentDeletion() {
		AdminShipmentService service = mock(AdminShipmentService.class);
		UserRepository users = mock(UserRepository.class);
		User manager = user("manager", "MANAGER");
		when(users.findByUsername("manager")).thenReturn(Optional.of(manager));
		AdminShipmentController controller = new AdminShipmentController(service, users);

		assertThrows(IllegalArgumentException.class, () -> controller.deleteCompletedShipment(3L, auth("manager")));

		verify(service, never()).deleteCompletedShipment(3L);
	}

	private User user(String username, String roleName) {
		User user = mock(User.class);
		when(user.getUsername()).thenReturn(username);
		when(user.getRole()).thenReturn(new Role(roleName));
		return user;
	}

	private UsernamePasswordAuthenticationToken auth(String username) {
		return UsernamePasswordAuthenticationToken.authenticated(username, "n/a", List.of());
	}
}
