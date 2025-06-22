package com.codetest.bookingsystem.dto.response;

import com.codetest.bookingsystem.enums.Country;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for a credit package")
public class CreditPackageResponse {

	@Schema(description = "ID of the credit package", example = "1")
	private Long id;

	@Schema(description = "Name of the package", example = "Basic Package SG")
	private String name;

	@Schema(description = "Number of credits included in the package", example = "5")
	private int credits;

	@Schema(description = "Price of the package", example = "49.99")
	private BigDecimal price;

	@Schema(description = "Country where this package is available", example = "SINGAPORE")
	private Country country;

	@Schema(description = "Validity period of the package in days", example = "30")
	private int validityInDays;
}