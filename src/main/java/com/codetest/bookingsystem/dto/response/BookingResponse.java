package com.codetest.bookingsystem.dto.response;

import java.time.Instant;

import com.codetest.bookingsystem.enums.BookingStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for a class booking")
public class BookingResponse {

	@Schema(description = "ID of the booking", example = "1")
	private Long id;

	@Schema(description = "Details of the user who made the booking")
	private UserResponse user;

	@Schema(description = "Details of the booked class schedule")
	private ClassScheduleResponse classSchedule;

	@Schema(description = "Details of the user package used for this booking")
	private UserPackageResponse userPackage;

	@Schema(description = "Current status of the booking", example = "BOOKED")
	private BookingStatus status;

	@Schema(description = "Timestamp when the booking was created", example = "2023-10-27T10:00:00Z")
	private Instant createdAt;

	@Schema(description = "Timestamp when the booking was last updated", example = "2023-10-27T10:05:00Z")
	private Instant updatedAt;
}
