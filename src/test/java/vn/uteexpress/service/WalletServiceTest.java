package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;

class WalletServiceTest {

	@Test
	void simulatedTopUpAddsFundsToLockedUserBalance() {
		UserRepository users = mock(UserRepository.class);
		WalletService service = new WalletService(users);
		User user = new User();
		user.setWalletBalance(new BigDecimal("25000"));
		when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(user));

		var wallet = service.topUp(7L, new BigDecimal("100000"));

		assertEquals(new BigDecimal("125000"), wallet.balance());
		assertEquals(new BigDecimal("125000"), user.getWalletBalance());
		verify(users).save(user);
	}

	@Test
	void rejectsInvalidTopUpBeforeLookingUpUser() {
		UserRepository users = mock(UserRepository.class);
		WalletService service = new WalletService(users);

		assertThrows(IllegalArgumentException.class,
				() -> service.topUp(7L, new BigDecimal("50000001")));
		assertThrows(IllegalArgumentException.class,
				() -> service.topUp(7L, new BigDecimal("1000.50")));

		verifyNoInteractions(users);
	}
}
