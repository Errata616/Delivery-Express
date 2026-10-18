package vn.uteexpress.config;

import vn.uteexpress.entity.Role;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.RoleRepository;
import vn.uteexpress.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

	@Bean
	CommandLineRunner initDatabase(RoleRepository roleRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {

		return args -> {

			// =========================
			// TẠO ROLE
			// =========================

			createRole(roleRepository, "USER");
			createRole(roleRepository, "VENDOR");
			createRole(roleRepository, "MANAGER");
			createRole(roleRepository, "SHIPPER");
			createRole(roleRepository, "ADMIN");

			// =========================
			// TẠO ADMIN
			// =========================

			if (!userRepository.existsByUsername("admin")) {

				Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

				User admin = new User();

				admin.setUsername("admin");
				admin.setEmail("admin@uteexpress.com");

				// Password thật: Admin@123
				admin.setPassword(passwordEncoder.encode("Admin@123"));

				admin.setFullName("UTEExpress Administrator");
				admin.setPhone("0900000000");
				admin.setEnabled(true);
				admin.setRole(adminRole);

				userRepository.save(admin);

				System.out.println("======================================");
				System.out.println(" ADMIN ACCOUNT CREATED");
				System.out.println(" Username: admin");
				System.out.println(" Password: Admin@123");
				System.out.println("======================================");
			}
		};
	}

	private void createRole(RoleRepository roleRepository, String roleName) {

		if (roleRepository.findByName(roleName).isEmpty()) {

			roleRepository.save(new Role(roleName));

			System.out.println("Created role: " + roleName);
		}
	}
}