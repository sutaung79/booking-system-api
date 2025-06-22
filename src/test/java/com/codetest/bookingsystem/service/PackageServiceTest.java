package com.codetest.bookingsystem.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.codetest.bookingsystem.dto.request.PackagePurchaseRequest;
import com.codetest.bookingsystem.dto.response.CreditPackageResponse;
import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.enums.Role;
import com.codetest.bookingsystem.exception.ResourceNotFoundException;
import com.codetest.bookingsystem.model.AppUser;
import com.codetest.bookingsystem.model.CreditPackage;
import com.codetest.bookingsystem.repository.AppUserRepository;
import com.codetest.bookingsystem.repository.CreditPackageRepository;
import com.codetest.bookingsystem.repository.UserPackageRepository;
import com.codetest.bookingsystem.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class PackageServiceTest {

	@Mock
	private CreditPackageRepository creditPackageRepository;
	@Mock
	private UserPackageRepository userPackageRepository;
	@Mock
	private AppUserRepository appUserRepository;
	@Mock
	private MockPaymentService mockPaymentService;
	@InjectMocks
	private PackageService packageService;

	private AppUser testUser;
	private UserDetailsImpl testUserDetails;
	private CreditPackage testCreditPackage;

	@BeforeEach
	void setUp() {
		testUser = new AppUser();
		testUser.setId(1L);
		testUser.setUsername("testuser");
		testUser.setRole(Role.USER); // Set the role to avoid NullPointerException

		testUserDetails = UserDetailsImpl.build(testUser);

		testCreditPackage = new CreditPackage();
		testCreditPackage.setId(1L);
		testCreditPackage.setName("Basic Package");
		testCreditPackage.setCredits(5);
		testCreditPackage.setPrice(BigDecimal.valueOf(49.99));
		testCreditPackage.setCountry(Country.SINGAPORE);
		testCreditPackage.setValidityInDays(30);

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
	void getAvailablePackages_ReturnsListOfCreditPackageResponses() {
		// Arrange
		when(creditPackageRepository.findByCountry(Country.SINGAPORE)).thenReturn(List.of(testCreditPackage));

		// Act
		List<CreditPackageResponse> packages = packageService.getAvailablePackages(Country.SINGAPORE);

		// Assert
		assertFalse(packages.isEmpty());
		assertEquals(1, packages.size());
		assertEquals(testCreditPackage.getName(), packages.get(0).getName());
	}

	@Test
	void purchasePackage_SuccessfullyPurchasesPackage() {
		// Arrange
		mockSecurityContext();
		when(appUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
		when(creditPackageRepository.findById(testCreditPackage.getId())).thenReturn(Optional.of(testCreditPackage));
		when(mockPaymentService.paymentCharge(any(), any(), any())).thenReturn(true);
		when(userPackageRepository.save(any())).thenAnswer(i -> i.getArguments()[0]); // Return the argument itself

		PackagePurchaseRequest purchaseRequest = new PackagePurchaseRequest(testCreditPackage.getId());

		// Act
		assertDoesNotThrow(() -> packageService.purchasePackage(purchaseRequest));

		// Assert
		verify(userPackageRepository, times(1)).save(any());
		verify(mockPaymentService, times(1)).paymentCharge(any(), any(), any());
	}

	@Test
	void purchasePackage_ThrowsExceptionWhenCreditPackageNotFound() {
		// Arrange
		mockSecurityContext();
		when(appUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
		when(creditPackageRepository.findById(any())).thenReturn(Optional.empty());

		PackagePurchaseRequest purchaseRequest = new PackagePurchaseRequest(testCreditPackage.getId());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> packageService.purchasePackage(purchaseRequest));
		verify(userPackageRepository, never()).save(any());
		verify(mockPaymentService, never()).paymentCharge(any(), any(), any());
	}
}