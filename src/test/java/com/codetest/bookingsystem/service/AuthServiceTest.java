package com.codetest.bookingsystem.service;

import com.codetest.bookingsystem.dto.request.RegistrationRequest;
import com.codetest.bookingsystem.enums.Role;
import com.codetest.bookingsystem.exception.BadRequestException;
import com.codetest.bookingsystem.model.AppUser;
import com.codetest.bookingsystem.repository.AppUserRepository;
import com.codetest.bookingsystem.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private AppUserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private MockEmailService mockEmailService;
	@InjectMocks
	private AuthService authService;

	private RegistrationRequest registrationRequest;

	@BeforeEach
	void setUp() {
		registrationRequest = new RegistrationRequest("testuser", "test@example.com", "password123");
	}

	@Test
	void registerUser_SuccessfullyRegistersUser() {
		// Arrange
		when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
		when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
		when(mockEmailService.sendVerifyEmail(any(), any(), any())).thenReturn(true);

		// Act
		authService.registerUser(registrationRequest);

		// Assert
		ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
		verify(userRepository, times(1)).save(userCaptor.capture());
		AppUser savedUser = userCaptor.getValue();

		assertEquals(registrationRequest.getUsername(), savedUser.getUsername());
		assertEquals(registrationRequest.getEmail(), savedUser.getEmail());
		assertEquals("encodedPassword", savedUser.getPassword());
		assertEquals(Role.USER, savedUser.getRole());
		assertTrue(savedUser.isEnabled());
		verify(mockEmailService, times(1)).sendVerifyEmail(any(), any(), any());
	}

	@Test
	void registerUser_ThrowsExceptionWhenUsernameExists() {
		// Arrange
		when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);

		// Act & Assert
		BadRequestException exception = assertThrows(BadRequestException.class, () -> {
			authService.registerUser(registrationRequest);
		});

		assertEquals("Error: Username is already taken!", exception.getMessage());
		verify(userRepository, never()).save(any());
		verify(mockEmailService, never()).sendVerifyEmail(any(), any(), any());
	}

	@Test
	void registerUser_ThrowsExceptionWhenEmailExists() {
		// Arrange
		when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
		when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

		// Act & Assert
		BadRequestException exception = assertThrows(BadRequestException.class, () -> {
			authService.registerUser(registrationRequest);
		});

		assertEquals("Error: Email is already in use!", exception.getMessage());
		verify(userRepository, never()).save(any());
		verify(mockEmailService, never()).sendVerifyEmail(any(), any(), any());
	}

	@Test
	void registerUser_ThrowsExceptionWhenEmailSendingFails() {
		// Arrange
		when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
		when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
		when(mockEmailService.sendVerifyEmail(any(), any(), any())).thenReturn(false);

		// Act & Assert
		BadRequestException exception = assertThrows(BadRequestException.class, () -> {
			authService.registerUser(registrationRequest);
		});

		assertEquals("User registered successfully, but failed to send verification email. Please contact support.",
				exception.getMessage());
		verify(userRepository, times(1)).save(any());
		verify(mockEmailService, times(1)).sendVerifyEmail(any(), any(), any());
	}
}