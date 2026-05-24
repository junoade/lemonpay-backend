package com.lemonpay.common.interfaces;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ErrorResponse> handleCoreException(CoreException e, HttpServletRequest request) {
        log.warn("CoreException: {}", e.getMessage());
        ErrorType errorType = e.getErrorType();
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ErrorResponse.of(errorType, e.getMessage(), request));
    }

    /** @Valid 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e,
                                                                   HttpServletRequest request) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", detail);
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorType.INVALID_REQUEST, detail, request));
    }

    /** PathVariable/RequestParam 타입 불일치 (e.g. UUID 형식 오류) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                                     HttpServletRequest request) {
        String detail = "'%s' 파라미터의 값이 올바르지 않습니다.".formatted(e.getName());
        log.warn("Type mismatch: {}", detail);
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorType.INVALID_REQUEST, detail, request));
    }

    /** JSON 파싱 오류 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadableException(HttpMessageNotReadableException e,
                                                                    HttpServletRequest request) {
        log.warn("HttpMessageNotReadable: {}", e.getMessage());
        String detail = "요청 본문을 읽을 수 없습니다.";
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorType.INVALID_REQUEST, detail, request));
    }

    /** 예상하지 못한 예외 */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Throwable e, HttpServletRequest request) {
        log.error("Unexpected error", e);
        String detail = "예상치 못한 예외가 발생했습니다.";
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(ErrorType.INTERNAL_SERVER_ERROR, detail, request));
    }

    /** 잘못된 sort 필드명이 넘어왔을 때 500 대신 400 반환 */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(PropertyReferenceException e,
                                                                          HttpServletRequest request) {
        log.warn("Invalid sort field: {}", e.getPropertyName());
        String detail = String.format("정렬 기준이 올바르지 않습니다 : %s", e.getPropertyName());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorType.INVALID_REQUEST, detail, request));
    }
}
