package com.lemonpay.member.interfaces.api;

import com.lemonpay.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "회원 관리 API 요청 및 응답 DTO")
public class MemberDto {

    @Schema(description = "회원 가입 요청")
    public record CreationRequest(
            @NotBlank @Email String email,
            @NotBlank String name,
            @NotBlank String phone
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String name
    ) {

    }

    @Schema(description = "회원 관리 공통 응답")
    public record MemberResponse(
            @Schema(description = "회원의 UUID")
            UUID id,
            @Schema(description = "회원 이메일")
            String email,
            @Schema(description = "회원 이름")
            String name,
            @Schema(description = "회원 전화번호")
            String phone,
            @Schema(description = "회원 상태")
            String status,
            @Schema(description = "회원 가입일자/시간")
            LocalDateTime createdAt,
            @Schema(description = "회원 정보 변경일자/시간")
            LocalDateTime updatedAt
    ) {

        public static MemberResponse from(Member member) {
            return new MemberResponse(
                    member.getId(),
                    member.getEmail(),
                    member.getName(),
                    member.getPhone(),
                    member.getStatus().name(),
                    member.getCreatedAt(),
                    member.getUpdatedAt()
            );
        }
    }
}
