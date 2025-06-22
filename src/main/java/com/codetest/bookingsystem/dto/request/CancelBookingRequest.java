package com.codetest.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for cancelling a booking")
public class CancelBookingRequest {

	@NotNull(message = "Booking ID cannot be null")
	@Schema(description = "ID of the booking to cancel", example = "1")
	private Long bookingId;
}