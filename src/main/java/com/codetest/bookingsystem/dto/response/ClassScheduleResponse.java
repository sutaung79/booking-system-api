package com.codetest.bookingsystem.dto.response;

import com.codetest.bookingsystem.enums.Country;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for a class schedule")
public class ClassScheduleResponse {

	@Schema(description = "ID of the class schedule", example = "1")
	private Long id;

	@Schema(description = "Name of the class", example = "1 hr Yoga Class")
	private String className;

	@Schema(description = "Start time of the class", example = "2023-11-01T09:00:00")
	private LocalDateTime startTime;

	@Schema(description = "End time of the class", example = "2023-11-01T10:00:00")
	private LocalDateTime endTime;

	@Schema(description = "Maximum capacity of the class", example = "10")
	private int capacity;

	@Schema(description = "Country where the class is held", example = "SINGAPORE")
	private Country country;

	@Schema(description = "Number of credits required to book this class", example = "1")
	private int requiredCredits;

	@Schema(description = "Current number of booked slots for this class", example = "5")
	private int currentBookedCount; // to show how many slots are taken
}