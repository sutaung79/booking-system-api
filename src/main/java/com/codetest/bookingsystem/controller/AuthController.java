package com.codetest.bookingsystem.controller;

import com.codetest.bookingsystem.dto.request.LoginRequest;
import com.codetest.bookingsystem.dto.request.RegistrationRequest;
import com.codetest.bookingsystem.dto.response.AuthResponse;
import com.codetest.bookingsystem.dto.response.MessageResponse;
import com.codetest.bookingsystem.dto.request.ResetPasswordRequest;
import com.codetest.bookingsystem.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {

	@Autowired
	private AuthService authService;

	@PostMapping("/login")
	@Operation(summary = "Authenticate user and get JWT token", description = "Authenticates a user with username/email and password, returning a JWT token upon success.")
	@ApiResponse(responseCode = "200", description = "User authenticated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid credentials, unverified account)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized (e.g., invalid username/password)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		AuthResponse authResponse = authService.loginUser(loginRequest);
		return ResponseEntity.ok(authResponse);
	}

	@PostMapping("/register")
	@Operation(summary = "Register a new user", description = "Registers a new user with username, email, and password. An email verification is required.")
	@ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
	@ApiResponse(responseCode = "400", description = "Bad request (e.g., username/email already taken, validation errors)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
		MessageResponse messageResponse = authService.registerUser(registrationRequest);
		return ResponseEntity.ok(messageResponse);
	}

	@PostMapping("/request-password-reset")
	@Operation(summary = "Request password reset", description = "Sends a password reset token to the user's email.")
	@ApiResponse(responseCode = "200", description = "Password reset link sent successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
	@ApiResponse(responseCode = "404", description = "User not found with provided email", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@ApiResponse(responseCode = "400", description = "Failed to send email", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<MessageResponse> requestPasswordReset(@RequestParam String email) {
		MessageResponse messageResponse = authService.requestPasswordReset(email);
		return ResponseEntity.ok(messageResponse);
	}

	@PostMapping("/reset-password")
	@Operation(summary = "Reset password using token", description = "Resets user's password using a valid token received via email.")
	@ApiResponse(responseCode = "200", description = "Password reset successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
	@ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<MessageResponse> resetPassword(@RequestParam String token,
			@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
		MessageResponse messageResponse = authService.resetPassword(token, resetPasswordRequest.getNewPassword());
		return ResponseEntity.ok(messageResponse);
	}
}