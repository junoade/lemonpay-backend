package com.lemonpay.common.interfaces;

import com.lemonpay.common.exception.ErrorType;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 추후 RFC 9457를 구현한 ProblemDetail로 변경을 고려할 수 있다.
 *
 * @param errorCode
 * @param message JSON string containing a human-readable explanation specific to this occurrence of the problem.
 * @param status JSON number indicating the HTTP status code.
 * @param title JSON string containing a short, human-readable summary of the problem type.
 * @param instance JSON string containing a URI reference that identifies the specific occurrence of the problem.
 */
public record ErrorResponse(
        String errorCode,
        String message,
        int status,
        String title,
        String instance
) {
    public static ErrorResponse of(ErrorType errorType, HttpServletRequest request) {
        return of(errorType, errorType.getMessage(), request);
    }

    public static ErrorResponse of(ErrorType errorType, String message, HttpServletRequest request) {
        return new ErrorResponse(
                errorType.name(),
                message,
                errorType.getStatus().value(),
                errorType.getStatus().getReasonPhrase(),
                request.getRequestURI()
        );
    }
}
