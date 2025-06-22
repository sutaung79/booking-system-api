package com.codetest.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for booking a class")
public class BookingRequest {

    @NotNull(message = "Class schedule ID cannot be null")
    @Schema(description = "ID of the class schedule to book", example = "1")
    private Long classScheduleId; // userPackageId can be optional if the system automatically picks the best package

}