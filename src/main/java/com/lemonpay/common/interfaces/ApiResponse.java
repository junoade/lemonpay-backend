package com.lemonpay.common.interfaces;

import java.time.OffsetDateTime;

public record ApiResponse<T>(MetaData meta, T data) {

    public record MetaData(String timestamp) {
        public static MetaData now() {
            return new MetaData(OffsetDateTime.now().toString());
        }
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(MetaData.now(), data);
    }

}
