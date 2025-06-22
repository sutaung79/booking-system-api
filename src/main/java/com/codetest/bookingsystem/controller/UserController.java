package com.codetest.bookingsystem.controller;

import com.codetest.bookingsystem.dto.request.ChangePasswordRequest;
import com.codetest.bookingsystem.dto.response.MessageResponse;
import com.codetest.bookingsystem.dto.response.UserResponse;
import com.codetest.bookingsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.tags.Tag(name = "User Management", description = "APIs for user profile and password management")
public class UserController {

	@Autowired
	private AuthService authService;

	@GetMapping("/profile")
	@Operation(summary = "Get authenticated user's profile", description = "Retrieves the profile details of the currently authenticated user.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved user profile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<UserResponse> getProfile() {
		UserResponse userProfile = authService.getUserProfile();
		return ResponseEntity.ok(userProfile);
	}

	@PostMapping("/change-password")
	@Operation(summary = "Change authenticated user's password", description = "Allows the authenticated user to change their password by providing the old and new passwords.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request (e.g., incorrect old password, validation errors)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<MessageResponse> changePassword(
			@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
		MessageResponse messageResponse = authService.changePassword(changePasswordRequest);
		return ResponseEntity.ok(messageResponse);
	}
}