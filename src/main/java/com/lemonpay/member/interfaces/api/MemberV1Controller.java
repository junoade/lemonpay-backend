package com.lemonpay.member.interfaces.api;


import com.lemonpay.common.interfaces.ApiResponse;
import com.lemonpay.member.domain.Member;
import com.lemonpay.member.domain.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberV1Controller implements MemberV1ApiSpec {

    private final MemberService memberService;

    @Override
    public ResponseEntity<ApiResponse<MemberDto.MemberResponse>> create(MemberDto.CreationRequest request) {
        Member member = memberService.joinMember(request.email(), request.name(), request.phone());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(MemberDto.MemberResponse.from(member)));
    }

    @Override
    public ResponseEntity<ApiResponse<MemberDto.MemberResponse>> login(MemberDto.LoginRequest request) {
        Member member = memberService.validateAccess(request.email(), request.name());
        return ResponseEntity.ok(ApiResponse.of(MemberDto.MemberResponse.from(member)));
    }
}
