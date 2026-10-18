package vn.uteexpress.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.RefreshToken;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

	// New raw token plus the username it belongs to
	public record Rotated(String newRawToken, String username) {
	}

	private static final SecureRandom RANDOM = new SecureRandom();

	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${app.jwt.refresh-expiration-ms:604800000}")
	private long refreshExpirationMs;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	// Returns the RAW token to hand to the client. Only its hash is stored.
	@Transactional
	public String create(User user) {

		String raw = generateRaw();

		refreshTokenRepository.save(
				new RefreshToken(hash(raw), user, LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs))));

		return raw;
	}

	// noRollbackFor: the "revoke" updates below must be saved even though we then throw
	@Transactional(noRollbackFor = BadCredentialsException.class)
	public Rotated rotate(String rawToken) {

		RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawToken))
				.orElseThrow(() -> new BadCredentialsException("Refresh token không hợp lệ hoặc đã hết hạn"));

		User user = stored.getUser();

		if (stored.isRevoked()) {
			// A used token came back: it may have been stolen, so end every session of this user
			refreshTokenRepository.revokeAllByUserId(user.getId());
			throw new BadCredentialsException("Refresh token không hợp lệ hoặc đã hết hạn");
		}

		if (stored.getExpiresAt().isBefore(LocalDateTime.now()) || !user.isEnabled()) {
			stored.setRevoked(true);
			refreshTokenRepository.save(stored);
			throw new BadCredentialsException("Refresh token không hợp lệ hoặc đã hết hạn");
		}

		stored.setRevoked(true);
		refreshTokenRepository.save(stored);

		return new Rotated(create(user), user.getUsername());
	}

	// Used by logout. Unknown tokens are ignored on purpose.
	@Transactional
	public void revoke(String rawToken) {

		refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
			token.setRevoked(true);
			refreshTokenRepository.save(token);
		});
	}

	private String generateRaw() {

		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);

		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String raw) {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}
}