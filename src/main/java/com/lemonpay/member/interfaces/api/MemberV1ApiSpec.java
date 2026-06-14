package com.lemonpay.member.interfaces.api;

import com.lemonpay.common.interfaces.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Member V1 API Specification")
public interface MemberV1ApiSpec {

    @PostMapping("/join")
    @Operation(
            summary = "회원가입 요청 API",
            description = "레몬페이 회원가입 요청 API"
    )
    ResponseEntity<ApiResponse<MemberDto.MemberResponse>> create(
            @Valid @RequestBody MemberDto.CreationRequest request
    );

    @PostMapping("/login")
    @Operation(
            summary = "로그인 요청 API",
            description = "레몬페이 로그인 요청 API. MVP에서는 간단하게만 구현"
    )
    ResponseEntity<ApiResponse<MemberDto.MemberResponse>> login(
            @Valid @RequestBody MemberDto.LoginRequest request
    );

    // TODO 회원정보 조회 / 수정 / 탈퇴
}
