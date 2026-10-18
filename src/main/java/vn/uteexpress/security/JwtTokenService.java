package vn.uteexpress.security;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import vn.uteexpress.config.JwtConfig;

@Service
public class JwtTokenService {

	private final JwtConfig jwtConfig;
	private final SecretKey signingKey;

	public JwtTokenService(JwtConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
		if (jwtConfig.getSecret() == null || jwtConfig.getSecret().length() < 32) {
			throw new IllegalStateException("JWT secret must be at least 32 characters long");
		}
		this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(UserDetails userDetails) {
		Date issuedAt = new Date();
		Date expiration = new Date(issuedAt.getTime() + jwtConfig.getExpirationMs());

		return Jwts.builder().subject(userDetails.getUsername()).issuer(jwtConfig.getIssuer()).issuedAt(issuedAt)
				.expiration(expiration).signWith(signingKey).compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		try {
			Claims claims = parseClaims(token);
			return userDetails.isEnabled()
					&& userDetails.isAccountNonLocked()
					&& claims.getSubject().equals(userDetails.getUsername())
					&& claims.getExpiration().after(new Date())
					&& jwtConfig.getIssuer().equals(claims.getIssuer());
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
	}
}
