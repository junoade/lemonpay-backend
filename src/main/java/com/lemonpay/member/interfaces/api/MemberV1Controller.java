package com.lemonpay.member.interfaces.api;


import com.lemonpay.common.interfaces.ApiResponse;
import com.lemonpay.member.application.MemberCommand;
import com.lemonpay.member.application.MemberResult;
import com.lemonpay.member.application.MemberUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberV1Controller implements MemberV1ApiSpec {

    private final MemberUsecase memberUsecase;

    @Override
    public ResponseEntity<ApiResponse<MemberDto.MemberResponse>> create(MemberDto.CreationRequest request) {
        MemberCommand.Join command = request.toCommand();
        MemberResult.CommonResult result = memberUsecase.join(command);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(MemberDto.MemberResponse.from(result)));
    }

    @Override
    public ResponseEntity<ApiResponse<MemberDto.MemberResponse>> login(MemberDto.LoginRequest request) {
        MemberResult.CommonResult result = memberUsecase.login(request.email(), request.name());
        return ResponseEntity.ok(ApiResponse.of(MemberDto.MemberResponse.from(result)));
    }
}
