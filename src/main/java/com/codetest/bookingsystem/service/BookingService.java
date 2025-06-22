package com.codetest.bookingsystem.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codetest.bookingsystem.dto.request.BookingRequest;
import com.codetest.bookingsystem.dto.request.WaitlistRequest;
import com.codetest.bookingsystem.dto.response.BookingResponse;
import com.codetest.bookingsystem.dto.response.ClassScheduleResponse;
import com.codetest.bookingsystem.dto.response.CreditPackageResponse;
import com.codetest.bookingsystem.dto.response.MessageResponse;
import com.codetest.bookingsystem.dto.response.UserPackageResponse;
import com.codetest.bookingsystem.dto.response.UserResponse;
import com.codetest.bookingsystem.dto.response.WaitlistResponse;
import com.codetest.bookingsystem.enums.BookingStatus;
import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.enums.WaitlistStatus;
import com.codetest.bookingsystem.exception.BadRequestException;
import com.codetest.bookingsystem.exception.ResourceNotFoundException;
import com.codetest.bookingsystem.model.AppUser;
import com.codetest.bookingsystem.model.Booking;
import com.codetest.bookingsystem.model.ClassSchedule;
import com.codetest.bookingsystem.model.CreditPackage;
import com.codetest.bookingsystem.model.UserPackage;
import com.codetest.bookingsystem.model.Waitlist;
import com.codetest.bookingsystem.repository.AppUserRepository;
import com.codetest.bookingsystem.repository.BookingRepository;
import com.codetest.bookingsystem.repository.ClassScheduleRepository;
import com.codetest.bookingsystem.repository.UserPackageRepository;
import com.codetest.bookingsystem.repository.WaitlistRepository;
import com.codetest.bookingsystem.security.UserDetailsImpl;

@Service
public class BookingService {

	@Autowired
	private ClassScheduleRepository classScheduleRepository;
	@Autowired
	private BookingRepository bookingRepository;
	@Autowired
	private UserPackageRepository userPackageRepository;
	@Autowired
	private WaitlistRepository waitlistRepository;
	@Autowired
	private AppUserRepository appUserRepository;
	@Autowired
	private DistributedLockService distributedLockService;

	public List<ClassScheduleResponse> getAvailableSchedules(Country country) {
		return classScheduleRepository.findByCountryAndStartTimeAfter(country, LocalDateTime.now()).stream()
				.map(this::convertToClassScheduleResponse).collect(Collectors.toList());
	}

	@Transactional
	public BookingResponse bookClass(BookingRequest request) {
		UserDetailsImpl userDetails = getCurrentUserDetails();
		Long userId = userDetails.getId();
		Long classScheduleId = request.getClassScheduleId();

		// Acquire lock to prevent race conditions on booking
		if (!distributedLockService.acquireLock(classScheduleId)) {
			throw new BadRequestException(
					"The class is currently being booked by another user. Please try again shortly.");
		}

		try {
			ClassSchedule classSchedule = classScheduleRepository.findById(classScheduleId)
					.orElseThrow(() -> new ResourceNotFoundException("Class schedule not found."));

			// Validations inside the lock
			validateUserNotAlreadyBookedOrWaitlisted(userId, classScheduleId);
			validateNoOverlappingBookings(userId, classSchedule.getStartTime(), classSchedule.getEndTime());

			int currentBookings = bookingRepository.countByClassScheduleIdAndStatus(classScheduleId,
					BookingStatus.BOOKED);
			if (currentBookings >= classSchedule.getCapacity()) {
				throw new BadRequestException("Class is full. You can join the waitlist.");
			}

			UserPackage packageToUse = findAndValidatePackageForBooking(userId, classSchedule);

			// Perform booking
			packageToUse.setRemainingCredits(packageToUse.getRemainingCredits() - classSchedule.getRequiredCredits());
			userPackageRepository.save(packageToUse);

			Booking booking = new Booking();
			booking.setUser(appUserRepository.getReferenceById(userId));
			booking.setClassSchedule(classSchedule);
			booking.setUserPackage(packageToUse);
			booking.setStatus(BookingStatus.BOOKED);
			Booking savedBooking = bookingRepository.save(booking);

			return convertToBookingResponse(savedBooking);

		} finally {
			// Always release the lock
			distributedLockService.releaseLock(classScheduleId);
		}
	}

	@Transactional // Add Transactional to keep session open for lazy loading
	public MessageResponse cancelBooking(Long bookingId) {
		UserDetailsImpl userDetails = getCurrentUserDetails();
		Long userId = userDetails.getId();
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

		if (!booking.getUser().getId().equals(userId)) {
			throw new BadRequestException("You are not authorized to cancel this booking.");
		}

		if (booking.getStatus() != BookingStatus.BOOKED) {
			throw new BadRequestException("This booking cannot be cancelled.");
		}

		// Refund logic: 4 hours before class start time
		boolean isRefundable = LocalDateTime.now().isBefore(booking.getClassSchedule().getStartTime().minusHours(4));
		if (isRefundable) {
			UserPackage userPackage = booking.getUserPackage();
			userPackage.setRemainingCredits(
					userPackage.getRemainingCredits() + booking.getClassSchedule().getRequiredCredits());
			userPackageRepository.save(userPackage);
		}

		booking.setStatus(BookingStatus.CANCELLED);
		bookingRepository.save(booking);

		// After cancellation, try to promote someone from the waitlist
		processWaitlistPromotion(booking.getClassSchedule());
		String message = "Booking cancelled successfully." + (isRefundable ? " Credits have been refunded."
				: " No credits were refunded due to late cancellation.");
		return new MessageResponse(message);
	}

	@Transactional
	public WaitlistResponse joinWaitlist(WaitlistRequest request) {
		UserDetailsImpl userDetails = getCurrentUserDetails();
		Long userId = userDetails.getId();
		Long classScheduleId = request.getClassScheduleId();
		ClassSchedule classSchedule = classScheduleRepository.findById(classScheduleId)
				.orElseThrow(() -> new ResourceNotFoundException("Class schedule not found."));

		// Validations
		int currentBookings = bookingRepository.countByClassScheduleIdAndStatus(classScheduleId, BookingStatus.BOOKED);
		if (currentBookings < classSchedule.getCapacity()) {
			throw new BadRequestException("Class is not full yet. You can book it directly.");
		}
		validateUserNotAlreadyBookedOrWaitlisted(userId, classScheduleId);
		UserPackage packageToUse = findAndValidatePackageForBooking(userId, classSchedule);

		// Deduct credits for waitlist spot
		packageToUse.setRemainingCredits(packageToUse.getRemainingCredits() - classSchedule.getRequiredCredits());
		userPackageRepository.save(packageToUse);
		Waitlist waitlist = new Waitlist();
		waitlist.setUser(appUserRepository.getReferenceById(userId));
		waitlist.setClassSchedule(classSchedule);
		waitlist.setUserPackage(packageToUse); // Store the package used for credit deduction
		waitlist.setStatus(WaitlistStatus.WAITING);
		Waitlist savedWaitlist = waitlistRepository.save(waitlist);
		return convertToWaitlistResponse(savedWaitlist);
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> getMyBookings() {
		UserDetailsImpl userDetails = getCurrentUserDetails();
		Long userId = userDetails.getId();
		return bookingRepository.findByUserId(userId).stream().map(this::convertToBookingResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public MessageResponse checkInClass(Long bookingId) {
		UserDetailsImpl userDetails = getCurrentUserDetails();
		Long userId = userDetails.getId();
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
		
		if (!booking.getUser().getId().equals(userId)) {
			throw new BadRequestException("You are not authorized to check into this class.");
		}

		if (booking.getStatus() != BookingStatus.BOOKED) {
			throw new BadRequestException("Only booked classes can be checked in.");
		}

		// Check if current time is within check-in window (e.g., 30 mins before to class end time)
		if (LocalDateTime.now().isBefore(booking.getClassSchedule().getStartTime().minusMinutes(30))
				|| LocalDateTime.now().isAfter(booking.getClassSchedule().getEndTime())) {
			throw new BadRequestException(
					"Check-in is only allowed 30 minutes before class starts and before class ends.");
		}
		booking.setStatus(BookingStatus.CHECKED_IN);
		bookingRepository.save(booking);
		return new MessageResponse("Successfully checked into the class.");
	}

	private void processWaitlistPromotion(ClassSchedule classSchedule) {
		waitlistRepository
				.findFirstByClassScheduleIdAndStatusOrderByCreatedAtAsc(classSchedule.getId(), WaitlistStatus.WAITING)
				.ifPresent(waitlistEntry -> {
					// Note: This promotion logic is simplified. In a real-world scenario, you'd want to handle cases
					// where the promoted user's package might have expired in the meantime.
					// The userPackageId is now stored in the Waitlist entity, but for promotion,
					// we still need to find an active package for the user.
					UserPackage packageToAssociate = userPackageRepository
							.findActivePackagesForBooking(waitlistEntry.getUser().getId(), classSchedule.getCountry(),
									0, LocalDate.now())
							.stream().findFirst().orElse(null);

					if (packageToAssociate != null) {
						Booking newBooking = new Booking();
						newBooking.setUser(waitlistEntry.getUser());
						newBooking.setClassSchedule(classSchedule);
						newBooking.setUserPackage(packageToAssociate);
						newBooking.setStatus(BookingStatus.BOOKED);
						bookingRepository.save(newBooking);

						waitlistEntry.setStatus(WaitlistStatus.PROMOTED_TO_BOOKING);
						waitlistRepository.save(waitlistEntry);

						// Here you would typically send a notification (e.g., email, push notification) to the promoted user.
					}
				});
	}

	// Validation and Helper Methods 
	private UserPackage findAndValidatePackageForBooking(Long userId, ClassSchedule classSchedule) {
		List<UserPackage> activePackages = userPackageRepository.findActivePackagesForBooking(userId,
				classSchedule.getCountry(), classSchedule.getRequiredCredits(), LocalDate.now());

		if (activePackages.isEmpty()) {
			throw new BadRequestException("No active package with sufficient credits found for this country.");
		}
		// The query already sorts by expiry date, so the first one is the one that expires soonest.
		return activePackages.get(0);
	}

	private void validateUserNotAlreadyBookedOrWaitlisted(Long userId, Long classScheduleId) {
		if (bookingRepository.findByUserIdAndClassScheduleId(userId, classScheduleId).isPresent()) {
			throw new BadRequestException("You have already booked this class.");
		}
		if (waitlistRepository.findByUserIdAndClassScheduleId(userId, classScheduleId).isPresent()) {
			throw new BadRequestException("You are already on the waitlist for this class.");
		}
	}

	private void validateNoOverlappingBookings(Long userId, LocalDateTime newStartTime, LocalDateTime newEndTime) {
		if (!bookingRepository.findOverlappingBookings(userId, newStartTime, newEndTime).isEmpty()) {
			throw new BadRequestException("You have another booking that overlaps with this class time.");
		}
	}

	private UserDetailsImpl getCurrentUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
		}
		return (UserDetailsImpl) authentication.getPrincipal();
	}
	

	// DTO Conversion Methods 
	private UserResponse convertToUserResponse(AppUser user) {
		if (user == null) {
			return null;
		}
		return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.isEnabled());
	}

	private ClassScheduleResponse convertToClassScheduleResponse(ClassSchedule schedule) {
		int currentBookedCount = bookingRepository.countByClassScheduleIdAndStatus(schedule.getId(),
				BookingStatus.BOOKED);
		return new ClassScheduleResponse(schedule.getId(), schedule.getClassName(), schedule.getStartTime(),
				schedule.getEndTime(), schedule.getCapacity(), schedule.getCountry(), schedule.getRequiredCredits(),
				currentBookedCount);
	}

	private CreditPackageResponse convertToCreditPackageResponse(CreditPackage creditPackage) {
		if (creditPackage == null) {
			return null;
		}
		return new CreditPackageResponse(creditPackage.getId(), creditPackage.getName(), creditPackage.getCredits(),
				creditPackage.getPrice(), creditPackage.getCountry(), creditPackage.getValidityInDays());
	}

	private UserPackageResponse convertToUserPackageResponse(UserPackage userPackage) {
		if (userPackage == null) {
			return null;
		}
		return new UserPackageResponse(userPackage.getId(), userPackage.getUser().getId(),
				convertToCreditPackageResponse(userPackage.getCreditPackage()), userPackage.getRemainingCredits(),
				userPackage.getPurchaseDate(), userPackage.getExpiryDate(),
				userPackage.getExpiryDate().isBefore(LocalDate.now()), userPackage.getCreatedAt());
	}

	private BookingResponse convertToBookingResponse(Booking booking) {
		return new BookingResponse(booking.getId(), convertToUserResponse(booking.getUser()),
				convertToClassScheduleResponse(booking.getClassSchedule()),
				convertToUserPackageResponse(booking.getUserPackage()), booking.getStatus(), booking.getCreatedAt(),
				booking.getUpdatedAt());
	}

	private WaitlistResponse convertToWaitlistResponse(Waitlist waitlist) {
		return new WaitlistResponse(waitlist.getId(), convertToUserResponse(waitlist.getUser()),
				convertToClassScheduleResponse(waitlist.getClassSchedule()), waitlist.getStatus(),
				waitlist.getCreatedAt());
	}
}