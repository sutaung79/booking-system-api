package com.codetest.bookingsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic message response DTO")
public class MessageResponse {

	@Schema(description = "A descriptive message", example = "Operation successful!")
	private String message;
}