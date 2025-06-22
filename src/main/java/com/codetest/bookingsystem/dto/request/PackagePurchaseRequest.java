package com.codetest.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for purchasing a package")
public class PackagePurchaseRequest {

	@NotNull(message = "Credit package ID cannot be null")
	@Schema(description = "ID of the credit package to purchase", example = "1")
	private Long creditPackageId;
}