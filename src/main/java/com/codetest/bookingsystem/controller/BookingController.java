package com.codetest.bookingsystem.controller;

import com.codetest.bookingsystem.dto.request.BookingRequest;
import com.codetest.bookingsystem.dto.request.WaitlistRequest;
import com.codetest.bookingsystem.dto.response.BookingResponse;
import com.codetest.bookingsystem.dto.response.ClassScheduleResponse;
import com.codetest.bookingsystem.dto.response.MessageResponse;
import com.codetest.bookingsystem.dto.response.WaitlistResponse;
import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/booking")
@PreAuthorize("hasRole('USER')") // All methods in this controller require USER role
@io.swagger.v3.oas.annotations.tags.Tag(name = "Booking & Schedule", description = "APIs for viewing class schedules, booking, cancelling, and joining waitlists")
public class BookingController {

	@Autowired
	private BookingService bookingService;

	@GetMapping("/schedules")
	@Operation(summary = "Get available class schedules by country", description = "Retrieves a list of upcoming class schedules for a specified country.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved schedules", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassScheduleResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid country parameter", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<List<ClassScheduleResponse>> getAvailableSchedules(@RequestParam Country country) {
		List<ClassScheduleResponse> schedules = bookingService.getAvailableSchedules(country);
		return ResponseEntity.ok(schedules);
	}

	@PostMapping("/book")
	@Operation(summary = "Book a class", description = "Allows a user to book a class using their available credits. Handles concurrency to prevent overbooking.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class booked successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request (e.g., class full, insufficient credits, overlapping booking)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class schedule not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<BookingResponse> bookClass(@Valid @RequestBody BookingRequest request) {
		BookingResponse bookingResponse = bookingService.bookClass(request);
		return ResponseEntity.ok(bookingResponse);
	}

	@PostMapping("/cancel/{bookingId}")
	@Operation(summary = "Cancel a booked class", description = "Cancels a user's booked class. Credits are refunded if cancelled 4 hours before class start time.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking cancelled successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request (e.g., booking already cancelled, not authorized)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@Parameter(name = "bookingId", description = "ID of the booking to cancel", required = true, example = "1")
	public ResponseEntity<MessageResponse> cancelBooking(@PathVariable Long bookingId) {
		MessageResponse messageResponse = bookingService.cancelBooking(bookingId);
		return ResponseEntity.ok(messageResponse);
	}

	@PostMapping("/waitlist")
	@Operation(summary = "Join a class waitlist", description = "Allows a user to join the waitlist for a full class. Credits are deducted and refunded if not promoted.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully joined waitlist", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WaitlistResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request (e.g., class not full, already on waitlist)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class schedule not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<WaitlistResponse> joinWaitlist(@Valid @RequestBody WaitlistRequest request) {
		WaitlistResponse waitlistResponse = bookingService.joinWaitlist(request);
		return ResponseEntity.ok(waitlistResponse);
	}

	@PostMapping("/check-in/{bookingId}")
	@Operation(summary = "Check-in to a booked class", description = "Allows a user to check into their booked class within the allowed time window.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully checked into class", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request (e.g., not booked, outside check-in window)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@Parameter(name = "bookingId", description = "ID of the booking to check-in", required = true, example = "1")
	public ResponseEntity<MessageResponse> checkInToClass(@PathVariable Long bookingId) {
		MessageResponse messageResponse = bookingService.checkInClass(bookingId);
		return ResponseEntity.ok(messageResponse);
	}

	@GetMapping("/my-bookings")
	@Operation(summary = "Get my bookings", description = "Retrieves a list of all bookings made by the authenticated user.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved bookings", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookingResponse.class)))
	public ResponseEntity<List<BookingResponse>> getMyBookings() {
		List<BookingResponse> bookings = bookingService.getMyBookings();
		return ResponseEntity.ok(bookings);
	}
}