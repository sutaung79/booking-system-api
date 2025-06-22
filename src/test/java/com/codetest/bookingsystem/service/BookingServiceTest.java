package com.codetest.bookingsystem.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.codetest.bookingsystem.dto.request.BookingRequest;
import com.codetest.bookingsystem.dto.response.ClassScheduleResponse;
import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.enums.Role;
import com.codetest.bookingsystem.exception.BadRequestException;
import com.codetest.bookingsystem.exception.ResourceNotFoundException;
import com.codetest.bookingsystem.model.CreditPackage;
import com.codetest.bookingsystem.model.AppUser;
import com.codetest.bookingsystem.model.ClassSchedule;
import com.codetest.bookingsystem.model.UserPackage;
import com.codetest.bookingsystem.repository.AppUserRepository;
import com.codetest.bookingsystem.repository.BookingRepository;
import com.codetest.bookingsystem.repository.ClassScheduleRepository;
import com.codetest.bookingsystem.repository.UserPackageRepository;
import com.codetest.bookingsystem.repository.WaitlistRepository;
import com.codetest.bookingsystem.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

	@Mock
	private ClassScheduleRepository classScheduleRepository;
	@Mock
	private BookingRepository bookingRepository;
	@Mock
	private UserPackageRepository userPackageRepository;
	@Mock
	private AppUserRepository appUserRepository;
	@Mock
	private DistributedLockService distributedLockService;
	@Mock
	private WaitlistRepository waitlistRepository; // Add this mock
	@InjectMocks
	private BookingService bookingService;

	private AppUser testUser;
	private UserDetailsImpl testUserDetails;
	private ClassSchedule testClassSchedule;
	private UserPackage testUserPackage;
	private BookingRequest bookingRequest;

	@BeforeEach
	void setUp() {
		testUser = new AppUser();
		testUser.setId(1L);
		testUser.setUsername("testuser");
		testUser.setRole(Role.USER); // Set the role to avoid NullPointerException
		testUserDetails = UserDetailsImpl.build(testUser);

		testClassSchedule = new ClassSchedule();
		testClassSchedule.setId(1L);
		testClassSchedule.setClassName("Yoga Class");
		testClassSchedule.setStartTime(LocalDateTime.now().plusHours(1));
		testClassSchedule.setEndTime(LocalDateTime.now().plusHours(2));
		testClassSchedule.setCapacity(10);
		testClassSchedule.setCountry(Country.SINGAPORE);
		testClassSchedule.setRequiredCredits(1);

		testUserPackage = new UserPackage();
		testUserPackage.setId(1L);
		testUserPackage.setRemainingCredits(5);
		testUserPackage.setUser(testUser);
		testUserPackage.setExpiryDate(LocalDate.now().plusDays(30));
		testUserPackage.setPurchaseDate(LocalDate.now());

		CreditPackage creditPackage = new CreditPackage();
		creditPackage.setId(1L);
		testUserPackage.setCreditPackage(creditPackage);

		bookingRequest = new BookingRequest(testClassSchedule.getId());
	}

	private void mockSecurityContext() {
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(testUserDetails);
		when(authentication.isAuthenticated()).thenReturn(true);
	}

	@Test
	void getAvailableSchedules_ReturnsListOfClassScheduleResponses() {
		// Arrange
		when(classScheduleRepository.findByCountryAndStartTimeAfter(any(), any()))
				.thenReturn(List.of(testClassSchedule));
		// Act
		List<ClassScheduleResponse> schedules = bookingService.getAvailableSchedules(Country.SINGAPORE);

		// Assert
		assertFalse(schedules.isEmpty());
		assertEquals(1, schedules.size());
		assertEquals(testClassSchedule.getClassName(), schedules.get(0).getClassName());
	}

	@Test
	void bookClass_SuccessfullyBooksClass() {
		// Arrange
		mockSecurityContext();
		when(distributedLockService.acquireLock(testClassSchedule.getId())).thenReturn(true);
		when(classScheduleRepository.findById(testClassSchedule.getId())).thenReturn(Optional.of(testClassSchedule));
		when(bookingRepository.findByUserIdAndClassScheduleId(testUser.getId(), testClassSchedule.getId()))
				.thenReturn(Optional.empty());
		when(waitlistRepository.findByUserIdAndClassScheduleId(testUser.getId(), testClassSchedule.getId()))
				.thenReturn(Optional.empty());
		when(userPackageRepository.findActivePackagesForBooking(anyLong(), any(Country.class), anyInt(), any()))
				.thenReturn(List.of(testUserPackage));
		when(appUserRepository.getReferenceById(testUser.getId())).thenReturn(testUser);
		when(bookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]); // Return the argument itself

		// Act
		assertDoesNotThrow(() -> bookingService.bookClass(bookingRequest));

		// Assert
		verify(userPackageRepository, times(1)).save(any());
		verify(bookingRepository, times(1)).save(any());
		verify(distributedLockService, times(1)).releaseLock(testClassSchedule.getId());
	}

	@Test
	void bookClass_ThrowsExceptionWhenClassIsAlreadyBooked() {
		// Arrange
		mockSecurityContext();
		when(distributedLockService.acquireLock(testClassSchedule.getId())).thenReturn(true);
		when(classScheduleRepository.findById(testClassSchedule.getId())).thenReturn(Optional.of(testClassSchedule));
		when(bookingRepository.findByUserIdAndClassScheduleId(testUser.getId(), testClassSchedule.getId()))
				.thenReturn(Optional.of(mock(com.codetest.bookingsystem.model.Booking.class)));

		// Act & Assert
		assertThrows(BadRequestException.class, () -> bookingService.bookClass(bookingRequest));
		verify(userPackageRepository, never()).save(any());
		verify(bookingRepository, never()).save(any());
		verify(distributedLockService, times(1)).releaseLock(testClassSchedule.getId());
	}

	@Test
	void bookClass_ThrowsExceptionWhenNoActivePackageFound() {
		// Arrange
		mockSecurityContext();
		when(distributedLockService.acquireLock(testClassSchedule.getId())).thenReturn(true);
		when(classScheduleRepository.findById(testClassSchedule.getId())).thenReturn(Optional.of(testClassSchedule));
		when(bookingRepository.findByUserIdAndClassScheduleId(testUser.getId(), testClassSchedule.getId()))
				.thenReturn(Optional.empty());
		when(waitlistRepository.findByUserIdAndClassScheduleId(testUser.getId(), testClassSchedule.getId()))
				.thenReturn(Optional.empty());
		when(userPackageRepository.findActivePackagesForBooking(anyLong(), any(Country.class), anyInt(), any()))
				.thenReturn(List.of()); // No active package found
		// Act & Assert
		assertThrows(BadRequestException.class, () -> bookingService.bookClass(bookingRequest));
		verify(userPackageRepository, never()).save(any());
		verify(bookingRepository, never()).save(any());
		verify(distributedLockService, times(1)).releaseLock(testClassSchedule.getId());
	}

	@Test
	void bookClass_ThrowsExceptionWhenClassScheduleNotFound() {
		// Arrange
		mockSecurityContext();
		when(distributedLockService.acquireLock(testClassSchedule.getId())).thenReturn(true);
		when(classScheduleRepository.findById(testClassSchedule.getId())).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> bookingService.bookClass(bookingRequest));
		verify(userPackageRepository, never()).save(any());
		verify(bookingRepository, never()).save(any());
		verify(distributedLockService, times(1)).releaseLock(testClassSchedule.getId());
	}

	@Test
	void bookClass_ThrowsExceptionWhenCannotAcquireLock() {
		// Arrange
		mockSecurityContext();
		when(distributedLockService.acquireLock(testClassSchedule.getId())).thenReturn(false);

		// Act & Assert
		assertThrows(BadRequestException.class, () -> bookingService.bookClass(bookingRequest));
		verify(classScheduleRepository, never()).findById(any());
		verify(userPackageRepository, never()).save(any());
		verify(bookingRepository, never()).save(any());
		verify(distributedLockService, never()).releaseLock(testClassSchedule.getId());
	}
}