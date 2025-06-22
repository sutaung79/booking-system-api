package com.codetest.bookingsystem.dto.response;

import com.codetest.bookingsystem.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for user authentication")
public class AuthResponse {

	@Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private String token;

	@Schema(description = "Username of the authenticated user", example = "john.doe")
	private String username;

	@Schema(description = "Email of the authenticated user", example = "john.doe@example.com")
	private String email;

	@Schema(description = "Role of the authenticated user", example = "USER")
	private Role role;

	@Schema(description = "ID of the authenticated user", example = "1")
	private Long userId;

	@Schema(description = "Authentication message", example = "User logged in successfully")
	private String message;

}