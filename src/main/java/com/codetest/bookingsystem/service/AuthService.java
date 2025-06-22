package com.codetest.bookingsystem.service;

import com.codetest.bookingsystem.dto.request.ChangePasswordRequest;
import com.codetest.bookingsystem.dto.request.LoginRequest;
import com.codetest.bookingsystem.dto.request.RegistrationRequest;
import com.codetest.bookingsystem.dto.response.AuthResponse;
import com.codetest.bookingsystem.dto.response.MessageResponse;
import com.codetest.bookingsystem.dto.response.UserResponse;
import com.codetest.bookingsystem.enums.Role;
import com.codetest.bookingsystem.exception.BadRequestException;
import com.codetest.bookingsystem.exception.ResourceNotFoundException;
import com.codetest.bookingsystem.model.AppUser;
import com.codetest.bookingsystem.model.PasswordResetToken;
import com.codetest.bookingsystem.repository.AppUserRepository;
import com.codetest.bookingsystem.security.JwtTokenProvider;
import com.codetest.bookingsystem.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private AppUserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private MockEmailService mockEmailService;

	@Autowired
	private com.codetest.bookingsystem.repository.PasswordResetTokenRepository passwordResetTokenRepository;

	@Transactional
	public MessageResponse registerUser(RegistrationRequest registrationRequest) {
		if (userRepository.existsByUsername(registrationRequest.getUsername())) {
			throw new BadRequestException("Error: Username is already taken!");
		}

		if (userRepository.existsByEmail(registrationRequest.getEmail())) {
			throw new BadRequestException("Error: Email is already in use!");
		}

		AppUser user = new AppUser();
		user.setUsername(registrationRequest.getUsername());
		user.setEmail(registrationRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
		user.setRole(Role.USER);
		user.setEnabled(true); // Temporarily set to true for testing, as email verification is mocked
		userRepository.save(user);
		boolean emailSent = mockEmailService.sendVerifyEmail(user.getEmail(), "Please verify your registration",
				"Verification link: http://yourapi.com/api/auth/verify?token=some_token");
		if (!emailSent) {
			throw new BadRequestException(
					"User registered successfully, but failed to send verification email. Please contact support.");
		}
		return new MessageResponse("User registered successfully! Please check your email to verify your account.");
	}

	public AuthResponse loginUser(LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtTokenProvider.generateJwtToken(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		if (!userDetails.isEnabled()) {
			throw new BadRequestException("User account is not verified. Please check your email.");
		}
		return new AuthResponse(jwt, userDetails.getUsername(), userDetails.getEmail(),
				userDetails.getAuthorities().stream() // Get authorities (e.g., "ROLE_USER")
						.map(item -> Role.valueOf(item.getAuthority().replace("ROLE_", ""))) // Remove "ROLE_" prefix before converting to enum													
						.findFirst().orElse(null),
				userDetails.getId(), "User logged in successfully");
	}

	@Transactional
	public MessageResponse changePassword(ChangePasswordRequest changePasswordRequest) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		Long userId = userDetails.getId();
		AppUser user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
			throw new BadRequestException("Incorrect old password.");
		}
		user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
		userRepository.save(user);
		return new MessageResponse("Password changed successfully.");
	}

	public UserResponse getUserProfile() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		AppUser user = userRepository.findById(userDetails.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.isEnabled());
	}

	@Transactional
	public MessageResponse requestPasswordReset(String email) {
		AppUser user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		// Invalidate any existing tokens for this user
		passwordResetTokenRepository.findByUserId(user.getId()).ifPresent(token -> {
			passwordResetTokenRepository.delete(token);
			passwordResetTokenRepository.flush(); // Ensure the delete operation is executed immediately
		});

		String token = UUID.randomUUID().toString();
		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setToken(token);
		resetToken.setUser(user);
		resetToken.setExpiryDate(Instant.now().plusSeconds(3600)); // Token valid for 1 hour
		passwordResetTokenRepository.save(resetToken);
		boolean emailSent = mockEmailService.sendVerifyEmail(user.getEmail(), "Password Reset Request",
				"To reset your password, use the following token: " + token + "\n"
						+ "Or click the link: http://yourapi.com/api/auth/reset-password?token=" + token + "&email="
						+ user.getEmail());
		if (!emailSent) {
			throw new BadRequestException("Failed to send password reset email. Please try again later.");
		}
		return new MessageResponse("Password reset link has been sent to your email.");
	}

	@Transactional
	public MessageResponse resetPassword(String token, String newPassword) {
		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
				.orElseThrow(() -> new BadRequestException("Invalid or expired password reset token."));
		if (resetToken.isExpired()) {
			throw new BadRequestException("Password reset token has expired. Please request a new one.");
		}
		AppUser user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		passwordResetTokenRepository.delete(resetToken); // Invalidate token after use
		return new MessageResponse("Password has been reset successfully.");
	}
}