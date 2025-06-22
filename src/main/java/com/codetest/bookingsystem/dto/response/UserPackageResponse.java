package com.codetest.bookingsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for a user's purchased package")
public class UserPackageResponse {

	@Schema(description = "ID of the user's package", example = "1")
	private Long id;

	@Schema(description = "ID of the user who owns this package", example = "1")
	private Long userId; // Only ID, not full user object to avoid circular dependency

	@Schema(description = "Details of the credit package purchased")
	private CreditPackageResponse creditPackage;

	@Schema(description = "Number of remaining credits in this package", example = "3")
	private int remainingCredits;

	@Schema(description = "Date when the package was purchased", example = "2023-09-01")
	private LocalDate purchaseDate;

	@Schema(description = "Date when the package expires", example = "2023-10-01")
	private LocalDate expiryDate;

	@Schema(description = "Indicates if the package has expired", example = "true")
	private boolean expired; // Derived field for UI

	@Schema(description = "Timestamp when the user package record was created", example = "2023-09-01T10:00:00Z")
	private Instant createdAt;
}