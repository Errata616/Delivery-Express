package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.ShipperZoneRepository;
import vn.uteexpress.repository.UserRepository;

class ShipperZoneTeamScopeTest {

	@Test
	void managerListsOnlyTheirAssignedShippers() {
		User manager = user(12L, "MANAGER");
		User shipper = user(21L, "SHIPPER");
		UserRepository users = mock(UserRepository.class);
		when(users.findByManager_Id(12L)).thenReturn(List.of(shipper));
		ShipperZoneService service = service(users, mock(ShipperZoneRepository.class));

		var result = service.listAll(manager);

		assertEquals(List.of(21L), result.stream().map(response -> response.shipperId()).toList());
		verify(users).findByManager_Id(12L);
		verify(users, never()).findByRoleName("SHIPPER");
	}

	@Test
	void managerCannotEditZonesForShipperOutsideTheirTeam() {
		User manager = user(12L, "MANAGER");
		User otherManager = user(13L, "MANAGER");
		User shipper = user(21L, "SHIPPER");
		when(shipper.getManager()).thenReturn(otherManager);
		UserRepository users = mock(UserRepository.class);
		ShipperZoneRepository zones = mock(ShipperZoneRepository.class);
		when(users.findById(21L)).thenReturn(java.util.Optional.of(shipper));
		ShipperZoneService service = service(users, zones);

		assertThrows(AccessDeniedException.class, () -> service.replaceZones(21L, List.of(), manager));

		verify(zones, never()).findByShipperId(21L);
		verify(zones, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
	}

	private static ShipperZoneService service(UserRepository users, ShipperZoneRepository zones) {
		return new ShipperZoneService(zones, users, mock(ShipmentRepository.class), mock(RouteService.class));
	}

	private static User user(long id, String roleName) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(id);
		when(user.getRole()).thenReturn(new Role(roleName));
		when(user.isEnabled()).thenReturn(true);
		when(user.isAvailable()).thenReturn(true);
		return user;
	}
}
