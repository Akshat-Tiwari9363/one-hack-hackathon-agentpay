package com.agentpay.exception;

import com.agentpay.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SpendingLimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleSpendingLimitExceeded(SpendingLimitExceededException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                ex.getRequestId(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "SPENDING_LIMIT_EXCEEDED",
                ex.getMessage(),
                ex.getRequestedAmountPaise(),
                ex.getRemainingAmountPaise(),
                ex.getEnforcementLayer()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                null,
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                null,
                null,
                "APPLICATION"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");

        ErrorResponseDto error = new ErrorResponseDto(
                null,
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                msg,
                null,
                null,
                "APPLICATION"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMalformedJson(HttpMessageNotReadableException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                null,
                HttpStatus.BAD_REQUEST.value(),
                "MALFORMED_JSON",
                "Malformed request payload",
                null,
                null,
                "APPLICATION"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                null,
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENT",
                ex.getMessage(),
                null,
                null,
                "APPLICATION"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                null,
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "METHOD_NOT_ALLOWED",
                ex.getMessage(),
                null,
                null,
                "APPLICATION"
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                null,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                null,
                null,
                "APPLICATION"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
