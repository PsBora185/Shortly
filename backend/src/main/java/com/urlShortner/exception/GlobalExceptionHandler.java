package com.urlShortner.exception;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler({ BadRequestException.class, MethodArgumentNotValidException.class, BindException.class })
	public ResponseEntity<Map<String, Object>> badRequest(Exception exception) {
		if (exception instanceof MethodArgumentNotValidException validationException) {
			String message = validationException.getBindingResult()
					.getFieldErrors()
					.stream()
					.sorted(Comparator.comparing(FieldError::getField))
					.map(error -> error.getField() + ": " + error.getDefaultMessage())
					.findFirst()
					.orElse("Invalid request");
			return build(HttpStatus.BAD_REQUEST, "Bad Request", message);
		}
		if (exception instanceof BindException bindException) {
			String message = bindException.getBindingResult()
					.getFieldErrors()
					.stream()
					.sorted(Comparator.comparing(FieldError::getField))
					.map(error -> error.getField() + ": " + error.getDefaultMessage())
					.findFirst()
					.orElse("Invalid request");
			return build(HttpStatus.BAD_REQUEST, "Bad Request", message);
		}
		return build(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage());
	}

	@ExceptionHandler({ MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class })
	public ResponseEntity<Map<String, Object>> malformedRequest(Exception exception) {
		return build(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed request");
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<Map<String, Object>> conflict(ConflictException exception) {
		return build(HttpStatus.CONFLICT, "Conflict", exception.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> dataIntegrity(DataIntegrityViolationException exception) {
		return build(HttpStatus.CONFLICT, "Conflict", "The requested resource already exists");
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Map<String, Object>> badCredentials(BadCredentialsException exception) {
		return build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid email or password");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> accessDenied(AccessDeniedException exception) {
		return build(HttpStatus.FORBIDDEN, "Forbidden", "Access denied");
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException exception) {
		return build(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage());
	}

	@ExceptionHandler(GoneException.class)
	public ResponseEntity<Map<String, Object>> gone(GoneException exception) {
		return build(HttpStatus.GONE, "Gone", exception.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> illegalArgument(IllegalArgumentException exception) {
		return build(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> internalServerError(Exception exception) {
		exception.printStackTrace();
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String error, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", error);
		body.put("message", message);
		return ResponseEntity.status(status).body(body);
	}
}
