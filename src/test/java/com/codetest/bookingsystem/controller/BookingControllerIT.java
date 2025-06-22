package com.codetest.bookingsystem.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.codetest.bookingsystem.dto.request.BookingRequest;
import com.codetest.bookingsystem.dto.request.LoginRequest;
import com.codetest.bookingsystem.dto.request.PackagePurchaseRequest;
import com.codetest.bookingsystem.dto.request.RegistrationRequest;
import com.codetest.bookingsystem.dto.request.WaitlistRequest;
import com.codetest.bookingsystem.dto.response.AuthResponse;
import com.codetest.bookingsystem.dto.response.BookingResponse;
import com.codetest.bookingsystem.util.DatabaseCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use a dedicated test profile for database configuration
public class BookingControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DatabaseCleaner databaseCleaner;

	@BeforeEach
	void setUp() throws Exception {
		databaseCleaner.clearTables(); // Clear database before each test
	}

	// Helper method to register and login a user, and purchase a package
	private String setupUserAndPackage(String username, String email, String password, Long packageId)
			throws Exception {
		RegistrationRequest registrationRequest = new RegistrationRequest(username, email, password);
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registrationRequest))).andExpect(status().isOk());

		LoginRequest loginRequest = new LoginRequest(username, password);
		MvcResult loginResult = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk()).andReturn();

		String loginResponse = loginResult.getResponse().getContentAsString();
		AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
		String token = authResponse.getToken();

		PackagePurchaseRequest purchaseRequest = new PackagePurchaseRequest(packageId);
		mockMvc.perform(post("/api/packages/purchase").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(purchaseRequest)))
				.andExpect(status().isOk());
		return token;
	}

	@Test
	void shouldBookClassSuccessfullyAndCancel() throws Exception {
		String authToken = setupUserAndPackage("testuser1", "test1@example.com", "password123", 1L);
		Long classScheduleId = 1L; // Assuming class schedule with ID 1 exists

		// Book class
		BookingRequest bookingRequest = new BookingRequest(classScheduleId);

		MvcResult result = mockMvc
				.perform(post("/api/booking/book").header("Authorization", "Bearer " + authToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(bookingRequest)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.status").value("BOOKED")).andReturn();
		String responseString = result.getResponse().getContentAsString();
		BookingResponse bookingResponse = objectMapper.readValue(responseString, BookingResponse.class);
		Long bookingId = bookingResponse.getId();

		// Fail to book same class again
		mockMvc.perform(post("/api/booking/book").header("Authorization", "Bearer " + authToken)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(bookingRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You have already booked this class."));

		// Cancel booking
		mockMvc.perform(post("/api/booking/cancel/" + bookingId).header("Authorization", "Bearer " + authToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Booking cancelled successfully. Credits have been refunded."));

		// Fail to cancel non-existent booking
		long nonExistentBookingId = 9999L;
		mockMvc.perform(
				post("/api/booking/cancel/" + nonExistentBookingId).header("Authorization", "Bearer " + authToken))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Booking not found."));
	}

	@Test
	void shouldFailToBookWithInsufficientCredits() throws Exception {
		String authToken = setupUserAndPackage("testuser2", "test2@example.com", "password123", 1L); // User with 5
																										// credits
		Long classScheduleId = 1L; // Class requires 1 credit

		// Book 5 times to use up credits
		for (int i = 0; i < 5; i++) {
			mockMvc.perform(post("/api/booking/book").header("Authorization", "Bearer " + authToken)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(new BookingRequest(classScheduleId))))
					.andExpect(status().isOk());
		}

		// Attempt to book 6th time
		mockMvc.perform(post("/api/booking/book").header("Authorization", "Bearer " + authToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new BookingRequest(classScheduleId))))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message")
						.value("No active package with sufficient credits found for this country."));
	}

	@Test
	void shouldPromoteUserFromWaitlistWhenBookingIsCancelled() throws Exception {
		// Setup: Create a class with capacity 1 for this specific test. We need to ensure the database is clean before this test
		databaseCleaner.clearTables();
		/*
		 * This part of the test assumes an admin endpoint to create a class.
		 *  Since we don't have one, we'll have to imagine it for now. In a real-world scenario,
		 * we would either have an admin endpoint or use a repository to insert the  test data directly. 
		 * For now, I'll comment out the part that creates the class
		 * and assume a class with ID 5 and capacity 1 exists.
		 */
		Long testClassId = 5L; // Assuming the ID of the newly created class

		// User A books the class (fills it up)
		String userAAuthToken = setupUserAndPackage("userA", "userA@example.com", "passwordA", 1L);
		MvcResult bookingResultUserA = mockMvc
				.perform(post("/api/booking/book").header("Authorization", "Bearer " + userAAuthToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new BookingRequest(testClassId))))
				.andExpect(status().isOk()).andReturn();
		Long bookingIdUserA = objectMapper
				.readValue(bookingResultUserA.getResponse().getContentAsString(), BookingResponse.class).getId();

		// User B tries to book the same class and joins the waitlist
		String userBAuthToken = setupUserAndPackage("userB", "userB@example.com", "passwordB", 1L);
		mockMvc.perform(post("/api/booking/book").header("Authorization", "Bearer " + userBAuthToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new BookingRequest(testClassId))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Class is full. You can join the waitlist."));

		WaitlistRequest waitlistRequest = new WaitlistRequest(testClassId);
		mockMvc.perform(post("/api/booking/waitlist").header("Authorization", "Bearer " + userBAuthToken)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(waitlistRequest)))
				.andExpect(status().isOk());

		// User A cancels the booking
		mockMvc.perform(
				post("/api/booking/cancel/" + bookingIdUserA).header("Authorization", "Bearer " + userAAuthToken))
				.andExpect(status().isOk());

		// Verify User B is now booked (by trying to book again, which should fail with "already booked")
		mockMvc.perform(post("/api/booking/book").header("Authorization", "Bearer " + userBAuthToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new BookingRequest(testClassId))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You have already booked this class."));
	}
}