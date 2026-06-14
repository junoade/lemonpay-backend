package com.lemonpay.common.interfaces;

import java.time.OffsetDateTime;

public record ApiResponse<T>(Metadata meta, T data) {

    public record Metadata(String timestamp) {
        public static Metadata now() {
            return new Metadata(OffsetDateTime.now().toString());
        }
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(Metadata.now(), data);
    }

}
