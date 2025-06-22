package com.codetest.bookingsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for setting a new password during reset process")
public class ResetPasswordRequest {

	@NotBlank(message = "New password cannot be empty")
	@Schema(description = "New password for the user", example = "newPassword456")
	private String newPassword;
}