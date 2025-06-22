package com.codetest.bookingsystem.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException; // Import AccessDeniedException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * Handles custom ResourceNotFoundException. Triggered when a requested resource
	 * (e.g., user, class) is not found.
	 */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex,
			WebRequest request) {
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(),
				request.getDescription(false), HttpStatus.NOT_FOUND.value(), null);
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	/**
	 * Handles custom BadRequestException. Triggered for invalid business logic
	 * (e.g., insufficient credits, booking a full class).
	 */
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(),
				request.getDescription(false), HttpStatus.BAD_REQUEST.value(), null);
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handles Spring Security's AccessDeniedException. Triggered when an
	 * authenticated user tries to access a resource they don't have permission for.
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), "Access Denied",
				request.getDescription(false), HttpStatus.FORBIDDEN.value(), ex.getMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
	}

	/**
	 * Handles all other un-caught exceptions as a fallback. Returns a generic 500
	 * Internal Server Error.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), "An unexpected error occurred",
				request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Overrides the default handler for @Valid annotation failures. Provides a
	 * clear, structured response with all validation errors.
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> validationErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			validationErrors.put(fieldName, errorMessage);
		});
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), "Validation Failed",
				request.getDescription(false), HttpStatus.BAD_REQUEST.value(), validationErrors);
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}
}