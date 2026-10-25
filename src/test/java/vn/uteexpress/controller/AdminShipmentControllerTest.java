package vn.uteexpress.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.Shipment;
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

	@Test
	void managerReceivesOnlyTheirAssignedShippers() {
		AdminShipmentService service = mock(AdminShipmentService.class);
		UserRepository users = mock(UserRepository.class);
		User manager = user("manager", "MANAGER");
		when(manager.getId()).thenReturn(12L);
		User shipper = user("shipper", "SHIPPER");
		when(users.findByUsername("manager")).thenReturn(Optional.of(manager));
		when(users.findByManager_Id(12L)).thenReturn(List.of(shipper));
		AdminShipmentController controller = new AdminShipmentController(service, users);

		assertEquals(1, controller.getShippers(auth("manager")).getBody().size());

		verify(users).findByManager_Id(12L);
		verify(service, never()).getShippers();
	}

	@Test
	void managerSeesUnassignedShipmentsAndCurrentShipmentsOfTheirTeamOnly() {
		AdminShipmentService service = mock(AdminShipmentService.class);
		UserRepository users = mock(UserRepository.class);
		User manager = user("manager", "MANAGER");
		when(manager.getId()).thenReturn(12L);
		User ownShipper = user("own", "SHIPPER");
		when(ownShipper.getManager()).thenReturn(manager);
		User otherShipper = user("other", "SHIPPER");
		Shipment ownShipment = mock(Shipment.class);
		when(ownShipment.getShipper()).thenReturn(ownShipper);
		Shipment otherShipment = mock(Shipment.class);
		when(otherShipment.getShipper()).thenReturn(otherShipper);
		Shipment unassignedShipment = mock(Shipment.class);
		when(unassignedShipment.getShipper()).thenReturn(null);
		when(users.findByUsername("manager")).thenReturn(Optional.of(manager));
		when(service.getCurrentShipments()).thenReturn(List.of(ownShipment, otherShipment, unassignedShipment));
		when(service.filterHiddenShipments(12L, List.of(ownShipment, unassignedShipment)))
				.thenReturn(List.of(ownShipment, unassignedShipment));
		AdminShipmentController controller = new AdminShipmentController(service, users);

		List<Shipment> result = controller.getCurrentShipments(auth("manager")).getBody();

		assertEquals(List.of(ownShipment, unassignedShipment), result);
		verify(service).filterHiddenShipments(12L, List.of(ownShipment, unassignedShipment));
	}

	@Test
	void adminSeesAllCurrentShipments() {
		AdminShipmentService service = mock(AdminShipmentService.class);
		UserRepository users = mock(UserRepository.class);
		User admin = user("admin", "ADMIN");
		List<Shipment> shipments = List.of(mock(Shipment.class), mock(Shipment.class));
		when(users.findByUsername("admin")).thenReturn(Optional.of(admin));
		when(service.getCurrentShipments()).thenReturn(shipments);
		AdminShipmentController controller = new AdminShipmentController(service, users);

		assertEquals(shipments, controller.getCurrentShipments(auth("admin")).getBody());
		verify(service, never()).filterHiddenShipments(org.mockito.ArgumentMatchers.anyLong(),
				org.mockito.ArgumentMatchers.anyList());
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
