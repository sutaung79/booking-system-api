package com.codetest.bookingsystem.service;

import com.codetest.bookingsystem.dto.request.PackagePurchaseRequest;
import com.codetest.bookingsystem.dto.response.CreditPackageResponse;
import com.codetest.bookingsystem.dto.response.UserPackageResponse;
import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.exception.BadRequestException;
import com.codetest.bookingsystem.exception.ResourceNotFoundException;
import com.codetest.bookingsystem.model.AppUser;
import com.codetest.bookingsystem.model.CreditPackage;
import com.codetest.bookingsystem.model.UserPackage;
import com.codetest.bookingsystem.repository.AppUserRepository;
import com.codetest.bookingsystem.repository.CreditPackageRepository;
import com.codetest.bookingsystem.repository.UserPackageRepository;
import com.codetest.bookingsystem.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PackageService {

	@Autowired
	private CreditPackageRepository creditPackageRepository;

	@Autowired
	private UserPackageRepository userPackageRepository;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private MockPaymentService mockPaymentService;

	public List<CreditPackageResponse> getAvailablePackages(Country country) {
		return creditPackageRepository.findByCountry(country).stream().map(this::convertToCreditPackageResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public UserPackageResponse purchasePackage(PackagePurchaseRequest request) {
		UserDetailsImpl userDetails = getCurrentUserDetails();
		Long userId = userDetails.getId();
		AppUser user = appUserRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		
		CreditPackage creditPackage = creditPackageRepository.findById(request.getCreditPackageId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Credit Package not found with id: " + request.getCreditPackageId()));

		// Simulate payment charge
		boolean paymentSuccess = mockPaymentService.paymentCharge(UUID.randomUUID().toString(), // Mock transaction ID
				creditPackage.getPrice(), "SGD" // Assuming currency, can be enhanced
		);

		if (!paymentSuccess) {
			throw new BadRequestException("Payment failed. Please try again.");
		}

		UserPackage userPackage = new UserPackage();
		userPackage.setUser(user);
		userPackage.setCreditPackage(creditPackage);
		userPackage.setRemainingCredits(creditPackage.getCredits());
		userPackage.setPurchaseDate(LocalDate.now());
		userPackage.setExpiryDate(LocalDate.now().plusDays(creditPackage.getValidityInDays()));
		UserPackage savedUserPackage = userPackageRepository.save(userPackage);
		return convertToUserPackageResponse(savedUserPackage);
	}

	@Transactional // Ensure the session is open when accessing lazy-loaded creditPackage
	public List<UserPackageResponse> getMyPackages() {
		UserDetailsImpl userDetails = getCurrentUserDetails();
		Long userId = userDetails.getId();
		return userPackageRepository.findByUserId(userId).stream().map(this::convertToUserPackageResponse)
				.collect(Collectors.toList());
	}

	// Helper method to get current user
	private UserDetailsImpl getCurrentUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
		}
		return (UserDetailsImpl) authentication.getPrincipal();
	}

	// Mapper helper methods
	private UserPackageResponse convertToUserPackageResponse(UserPackage userPackage) {
		return new UserPackageResponse(userPackage.getId(), userPackage.getUser().getId(),
				convertToCreditPackageResponse(userPackage.getCreditPackage()), userPackage.getRemainingCredits(),
				userPackage.getPurchaseDate(), userPackage.getExpiryDate(),
				userPackage.getExpiryDate().isBefore(LocalDate.now()), // Calculate expired status
				userPackage.getCreatedAt());
	}

	private CreditPackageResponse convertToCreditPackageResponse(CreditPackage creditPackage) {
		return new CreditPackageResponse(creditPackage.getId(), creditPackage.getName(), creditPackage.getCredits(),
				creditPackage.getPrice(), creditPackage.getCountry(), creditPackage.getValidityInDays());
	}
}