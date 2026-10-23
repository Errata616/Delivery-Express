package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShipmentRepository;
import vn.uteexpress.repository.UserRepository;

class ShipperAvailabilityTest {

	@Test
	void shipperCanPauseAndResumeAvailabilityWithoutDisablingAccount() {
		UserRepository userRepository = mock(UserRepository.class);
		User shipper = new User("shipper", "shipper@example.com", "hash", "Shipper", null, true,
				new Role("SHIPPER"));
		when(userRepository.findById(10L)).thenReturn(Optional.of(shipper));
		ShipperService service = new ShipperService(mock(ShipmentRepository.class), userRepository,
				mock(ShipmentService.class));

		assertFalse(service.setAvailability(10L, false));
		assertFalse(shipper.isAvailable());
		assertTrue(shipper.isEnabled());
		assertTrue(service.setAvailability(10L, true));
		assertTrue(shipper.isAvailable());
		assertTrue(shipper.isEnabled());
		verify(userRepository, org.mockito.Mockito.times(2)).save(shipper);
	}

	@Test
	void availabilityCanOnlyBeChangedForShipperAccounts() {
		UserRepository userRepository = mock(UserRepository.class);
		User manager = new User("manager", "manager@example.com", "hash", "Manager", null, true,
				new Role("MANAGER"));
		when(userRepository.findById(20L)).thenReturn(Optional.of(manager));
		ShipperService service = new ShipperService(mock(ShipmentRepository.class), userRepository,
				mock(ShipmentService.class));

		assertThrows(IllegalArgumentException.class, () -> service.setAvailability(20L, false));
	}
}
