package com.codetest.bookingsystem.dto.request;

import com.codetest.bookingsystem.enums.Country;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ClassScheduleRequest {
	@NotBlank
	public String className;
	@NotNull
	@Future
	public LocalDateTime startTime;
	@NotNull
	@Future
	public LocalDateTime endTime;
	@Min(1)
	public int capacity;
	@NotNull
	public Country country;
	@Min(1)
	public int requiredCredits;

	public ClassScheduleRequest(String className, LocalDateTime startTime, LocalDateTime endTime, int capacity,
			Country country, int requiredCredits) {
		this.className = className;
		this.startTime = startTime;
		this.endTime = endTime;
		this.capacity = capacity;
		this.country = country;
		this.requiredCredits = requiredCredits;
	}
}