package com.codetest.bookingsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for user registration")
public class RegistrationRequest {

	@NotBlank(message = "Username cannot be empty")
	@Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
	@Schema(description = "Desired username for the new user", example = "newuser123")
	private String username;

	@NotBlank(message = "Email cannot be empty")
	@Email(message = "Invalid email format")
	@Schema(description = "Email address for the new user", example = "newuser@example.com")
	private String email;

	@NotBlank(message = "Password cannot be empty")
	@Size(min = 6, message = "Password must be at least 6 characters long")
	@Schema(description = "Password for the new user (min 6 characters)", example = "securepassword")
	private String password;
}