package com.codetest.bookingsystem.dto.response;

import com.codetest.bookingsystem.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for user profile information")
public class UserResponse {

	@Schema(description = "ID of the user", example = "1")
	private Long id;

	@Schema(description = "Username of the user", example = "john.doe")
	private String username;

	@Schema(description = "Email address of the user", example = "john.doe@example.com")
	private String email;

	@Schema(description = "Role of the user", example = "USER")
	private Role role;

	@Schema(description = "Indicates if the user's account is enabled (e.g., email verified)", example = "true")
	private boolean enabled;
}