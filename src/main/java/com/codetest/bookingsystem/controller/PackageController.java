package com.codetest.bookingsystem.controller;

import com.codetest.bookingsystem.dto.request.PackagePurchaseRequest;
import com.codetest.bookingsystem.dto.response.CreditPackageResponse;
import com.codetest.bookingsystem.dto.response.UserPackageResponse;
import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/packages")
@PreAuthorize("hasRole('USER')")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Package Management", description = "APIs for viewing and purchasing credit packages")
public class PackageController {

	@Autowired
	private PackageService packageService;

	@GetMapping("/available")
	@Operation(summary = "Get available credit packages by country", description = "Retrieves a list of credit packages available for purchase in a specified country.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved available packages", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditPackageResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid country parameter", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<List<CreditPackageResponse>> getAvailablePackages(@RequestParam Country country) {
		List<CreditPackageResponse> packages = packageService.getAvailablePackages(country);
		return ResponseEntity.ok(packages);
	}

	@PostMapping("/purchase")
	@Operation(summary = "Purchase a credit package", description = "Allows a user to purchase a specified credit package. Simulates a payment process.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Package purchased successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPackageResponse.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request (e.g., payment failed, invalid package ID)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Credit package not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.codetest.bookingsystem.exception.ErrorDetails.class)))
	public ResponseEntity<UserPackageResponse> purchasePackage(@Valid @RequestBody PackagePurchaseRequest request) {
		UserPackageResponse userPackage = packageService.purchasePackage(request);
		return ResponseEntity.ok(userPackage);
	}

	@GetMapping("/my-packages")
	@Operation(summary = "Get user's purchased packages", description = "Retrieves a list of all credit packages purchased by the authenticated user, including expired ones.")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved user's packages", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPackageResponse.class)))
	public ResponseEntity<List<UserPackageResponse>> getMyPackages() {
		List<UserPackageResponse> myPackages = packageService.getMyPackages();
		return ResponseEntity.ok(myPackages);
	}
}