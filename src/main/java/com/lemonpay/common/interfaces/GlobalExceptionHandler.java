package com.lemonpay.common.interfaces;

import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.common.exception.CoreException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public ResponseEntity<ErrorResponse> handleCoreException(CoreException e) {
        log.warn("CoreException: {}", e.getMessage());
        ErrorType errorType = e.getErrorType();
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ErrorResponse.of(errorType, e.getMessage()));
    }

    /** @Valid 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", detail);
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorType.INVALID_REQUEST, detail));
    }

    /** PathVariable/RequestParam 타입 불일치 (e.g. UUID 형식 오류) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String detail = "'%s' 파라미터의 값이 올바르지 않습니다.".formatted(e.getName());
        log.warn("Type mismatch: {}", detail);
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorType.INVALID_REQUEST, detail));
    }

    /** JSON 파싱 오류 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadable: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorType.INVALID_REQUEST, "요청 본문을 읽을 수 없습니다."));
    }

    /** 예상하지 못한 예외 */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Throwable e) {
        log.error("Unexpected error", e);
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(ErrorType.INTERNAL_SERVER_ERROR));
    }
}
