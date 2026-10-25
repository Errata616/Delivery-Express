package vn.uteexpress.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import vn.uteexpress.security.JwtAuthenticationEntryPoint;
import vn.uteexpress.security.JwtAuthenticationFilter;
import vn.uteexpress.security.JwtAccessDeniedHandler;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) {

		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/**")
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint)
						.accessDeniedHandler(jwtAccessDeniedHandler))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/shipments/tracking/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/shipments/public-quote").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/hubs/**").authenticated()
						.requestMatchers("/api/hubs/**").hasRole("ADMIN")
						.requestMatchers("/api/admin/users/**").hasRole("ADMIN")
						.requestMatchers("/api/admin/shipments/deletable").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/admin/shipments/**").hasRole("ADMIN")
						.requestMatchers("/api/shipments/**").authenticated()
						.requestMatchers("/api/shipper/**").hasAnyRole("SHIPPER", "ADMIN")
						.requestMatchers("/api/admin/shipments/**").hasAnyRole("ADMIN", "MANAGER")
						.requestMatchers(HttpMethod.POST, "/api/payments/webhook").permitAll()
						.requestMatchers("/api/payments/**").authenticated()
						.requestMatchers("/api/statistics/**").hasAnyRole("ADMIN", "MANAGER")
						.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register", "/api/auth/refresh",
								"/api/auth/logout")
						.permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        
		http
		.csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**"))
        .headers(h -> h.frameOptions(f -> f.sameOrigin()))
		.authorizeHttpRequests(auth -> auth
				// JSP FORWARD / ERROR
				.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
				// PUBLIC WEB
				.requestMatchers("/ws/**").permitAll()
				.requestMatchers("/", "/login", "/register", "/about-us", "/my-orders", "/booking", "/js/**", "/css/**",
						"/uploads/avatars/**").permitAll()
				// PAGE SHELLS: no data in them. admin.js/auth.js guard the view and the
				// /api/** chain enforces the real role checks on every data request.
				.requestMatchers("/admin/dashboard", "/manager/dashboard", "/shipper/dashboard").permitAll()
				//TRACKING
				.requestMatchers(HttpMethod.GET, "/api/routes/**").permitAll()
				// ADMIN WEB
				.requestMatchers("/admin/**").hasRole("ADMIN")
				// ADMIN WEB
				.requestMatchers("/admin/**").hasRole("ADMIN")
				// MANAGER WEB
				.requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
				// VENDOR WEB
				.requestMatchers("/vendor/**").hasAnyRole("VENDOR", "ADMIN")
				// SHIPPER WEB
				.requestMatchers("/shipper/**").hasAnyRole("SHIPPER", "ADMIN")
				.anyRequest().authenticated())
				

				// =========================
				// LOGIN
				// =========================

				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login").defaultSuccessUrl("/", true)
						.failureUrl("/login?error=true").permitAll())

				// =========================
				// LOGOUT
				// =========================

				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true").permitAll());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}