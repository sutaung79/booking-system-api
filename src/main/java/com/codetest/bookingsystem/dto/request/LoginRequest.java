package com.codetest.bookingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for user login")
public class LoginRequest {

	@Schema(description = "Username or email of the user", example = "john.doe")
	@NotBlank(message = "Username or email cannot be empty")
	private String usernameOrEmail;

	@Schema(description = "Password of the user", example = "password123")
	@NotBlank(message = "Password cannot be empty")
	private String password;
}