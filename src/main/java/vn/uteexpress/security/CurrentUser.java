package vn.uteexpress.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;

@Component
public class CurrentUser {

	private final UserRepository userRepository;

	public CurrentUser(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			throw new AuthenticationCredentialsNotFoundException("No authenticated user is available");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new UsernameNotFoundException("Authenticated user no longer exists"));
	}
}
