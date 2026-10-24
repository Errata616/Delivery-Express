package vn.uteexpress.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.dto.WalletResponse;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;

@Service
public class WalletService {

	private static final BigDecimal MAX_TOP_UP = BigDecimal.valueOf(50_000_000);

	private final UserRepository userRepository;

	public WalletService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public WalletResponse getWallet(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
		return new WalletResponse(user.getWalletBalance());
	}

	@Transactional
	public WalletResponse topUp(Long userId, BigDecimal amount) {
		if (userId == null || amount == null || amount.signum() <= 0
				|| amount.stripTrailingZeros().scale() > 0 || amount.compareTo(MAX_TOP_UP) > 0) {
			throw new IllegalArgumentException("Số tiền nạp phải là số nguyên từ 1 đến 50.000.000 VND");
		}

		User user = userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
		user.setWalletBalance(user.getWalletBalance().add(amount));
		userRepository.save(user);
		return new WalletResponse(user.getWalletBalance());
	}
}
