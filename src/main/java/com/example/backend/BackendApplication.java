package com.example.backend;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedDatabase(UserRepository userRepository) {
		return args -> {
			seedUser(userRepository, "9876543210", User.Role.SEEKER);
			seedUser(userRepository, "9988776655", User.Role.SEEKER);
			seedUser(userRepository, "8888888888", User.Role.MERCHANT);
			seedUser(userRepository, "8765432100", User.Role.MERCHANT);
			seedUser(userRepository, "7777777777", User.Role.BRAND);
		};
	}

	private void seedUser(UserRepository repository, String phone, User.Role role) {
		if (repository.findByPhoneNumber(phone).isEmpty()) {
			User user = new User();
			user.setPhoneNumber(phone);
			user.setRole(role);
			repository.save(user);
			System.out.println("Seeded test user: " + phone + " with role: " + role);
		}
	}
}
