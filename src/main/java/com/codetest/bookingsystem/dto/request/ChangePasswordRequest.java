package com.codetest.bookingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for changing user password")
public class ChangePasswordRequest {

	@NotBlank(message = "Old password cannot be empty")
	@Schema(description = "Current password of the user", example = "oldPassword123")
	private String oldPassword;

	@NotBlank(message = "New password cannot be empty")
	@Schema(description = "New password for the user", example = "newPassword456")
	private String newPassword;
}