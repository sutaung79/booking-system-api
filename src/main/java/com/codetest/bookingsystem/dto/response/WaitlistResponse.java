package com.codetest.bookingsystem.dto.response;

import com.codetest.bookingsystem.enums.WaitlistStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for a waitlist entry")
public class WaitlistResponse {

	@Schema(description = "ID of the waitlist entry", example = "1")
	private Long id;

	@Schema(description = "Details of the user on the waitlist")
	private UserResponse user;

	@Schema(description = "Details of the class schedule for which the user is waitlisted")
	private ClassScheduleResponse classSchedule;

	@Schema(description = "Current status of the waitlist entry", example = "WAITING")
	private WaitlistStatus status;

	@Schema(description = "Timestamp when the waitlist entry was created", example = "2023-10-27T10:00:00Z")
	private Instant createdAt;
}