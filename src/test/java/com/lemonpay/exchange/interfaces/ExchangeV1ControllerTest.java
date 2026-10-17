package com.lemonpay.exchange.interfaces;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.lemonpay.common.auth.UserContextHolder;
import com.lemonpay.common.auth.UserContextInterceptor;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.common.interfaces.GlobalExceptionHandler;
import com.lemonpay.config.OpenApiConfig;
import com.lemonpay.exchange.application.ExchangeCommand;
import com.lemonpay.exchange.application.ExchangeResult;
import com.lemonpay.exchange.application.ExchangeUseCase;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExchangeV1ControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WALLET_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String BODY = """
            {"walletId":"%s","baseCurrency":"USD","targetCurrency":"KRW",
             "baseAmount":100.00,"idempotencyKey":"exchange-request-001"}
            """.formatted(WALLET_ID);

    @Mock
    private ExchangeUseCase useCase;

    private ExchangeV1Controller controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new ExchangeV1Controller(useCase);
        var objectMapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(new UserContextInterceptor())
                .build();
    }

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("인증된 환전 JSON 요청을 Command로 전달하고 환전 결과를 ApiResponse로 반환한다.")
    void exchanges() throws Exception {
        ExchangeCommand expected = new ExchangeCommand(WALLET_ID, Currency.USD, Currency.KRW,
                new BigDecimal("100.00"), "exchange-request-001");
        ExchangeResult result = new ExchangeResult(WALLET_ID,
                Money.usd("100.00"), Money.won(135_000), Money.usd("900.00"), Money.won(185_000),
                new BigDecimal("1350.00000000"), LocalDate.of(2026, 10, 13), 1,
                ExchangeRateType.OFFICIAL, ExchangeRateSource.API, "exchange-request-001");
        when(useCase.exchange(expected)).thenAnswer(invocation -> {
            assertThat(UserContextHolder.getUserId()).isEqualTo(USER_ID);
            return result;
        });

        mockMvc.perform(post("/api/v1/exchanges")
                        .header("X-USER-ID", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.data.walletId").value(WALLET_ID.toString()))
                .andExpect(jsonPath("$.data.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.data.baseAmount").value(100.0))
                .andExpect(jsonPath("$.data.targetCurrency").value("KRW"))
                .andExpect(jsonPath("$.data.targetAmount").value(135000))
                .andExpect(jsonPath("$.data.baseBalanceAfter").value(900.0))
                .andExpect(jsonPath("$.data.targetBalanceAfter").value(185000))
                .andExpect(jsonPath("$.data.appliedRate").value(1350.0))
                .andExpect(jsonPath("$.data.rateDate").value("2026-10-13"))
                .andExpect(jsonPath("$.data.roundNo").value(1))
                .andExpect(jsonPath("$.data.rateType").value("OFFICIAL"))
                .andExpect(jsonPath("$.data.rateSource").value("API"))
                .andExpect(jsonPath("$.data.idempotencyKey").value("exchange-request-001"));

        verify(useCase).exchange(expected);
        assertThat(UserContextHolder.get()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "null"})
    @DisplayName("환전 금액이 없거나 0·음수이면 400을 반환하고 UseCase를 호출하지 않는다.")
    void rejectsInvalidAmount(String amount) throws Exception {
        assertBadRequest(BODY.replace("100.00", amount));
    }

    @Test
    @DisplayName("지원하지 않는 통화 문자열이면 400을 반환하고 UseCase를 호출하지 않는다.")
    void rejectsInvalidCurrency() throws Exception {
        assertBadRequest(BODY.replace("USD", "UNKNOWN"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})
    @DisplayName("환전 요청 키가 비어 있거나 100자를 초과하면 400을 반환하고 UseCase를 호출하지 않는다.")
    void rejectsInvalidKey(String key) throws Exception {
        assertBadRequest(BODY.replace("exchange-request-001", key));
    }

    @Test
    @DisplayName("사용자 헤더가 없으면 401을 반환하고 UseCase를 호출하지 않는다.")
    void rejectsMissingUser() throws Exception {
        mockMvc.perform(post("/api/v1/exchanges").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("UseCase의 잔액 락 충돌 오류를 기존 오류 형식과 HTTP 409로 반환한다.")
    void reportsLockConflict() throws Exception {
        when(useCase.exchange(any())).thenThrow(new CoreException(ErrorType.BALANCE_LOCK_CONFLICT));
        mockMvc.perform(post("/api/v1/exchanges").header("X-USER-ID", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("BALANCE_LOCK_CONFLICT"));
    }

    @Test
    @DisplayName("Swagger의 환전 API 문서에 필수 X-USER-ID 헤더 입력을 연결한다.")
    void documentsUserHeader() throws Exception {
        HandlerMethod handler = new HandlerMethod(controller,
                ExchangeV1Controller.class.getMethod("exchange", ExchangeDto.ExchangeRequest.class));
        Operation operation = new OpenApiConfig().userIdHeaderCustomizer().customize(new Operation(), handler);
        assertThat(operation.getParameters()).singleElement()
                .satisfies(parameter -> assertThat(parameter.get$ref())
                        .isEqualTo("#/components/parameters/XUserIdHeader"));
        var header = new OpenApiConfig().openAPI().getComponents().getParameters().get("XUserIdHeader");
        assertThat(header.getName()).isEqualTo("X-USER-ID");
        assertThat(header.getRequired()).isTrue();
    }

    private void assertBadRequest(String body) throws Exception {
        mockMvc.perform(post("/api/v1/exchanges").header("X-USER-ID", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        verifyNoInteractions(useCase);
    }
}
