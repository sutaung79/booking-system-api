package com.codetest.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for joining a class waitlist")
public class WaitlistRequest {

	@NotNull(message = "Class schedule ID cannot be null")
	@Schema(description = "ID of the class schedule to join the waitlist for", example = "1")
	private Long classScheduleId;
}