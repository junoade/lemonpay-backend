package com.lemonpay.config;

import com.lemonpay.payment.interfaces.api.PaymentV1Controller;
import com.lemonpay.wallet.interfaces.api.WalletV1Controller;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LemonPay API")
                        .version("v1")
                        .description("LemonPay API DOCS")
                )
                .components(new Components()
                        .addParameters("XUserIdHeader",
                                new HeaderParameter()
                                        .name("X-USER-ID")
                                        .description("토이 프로젝트용 사용자 식별 헤더")
                                        .required(true)
                                        .schema(new StringSchema())
                        )
                );
    }

    @Bean
    public OperationCustomizer userIdHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            if (requiresUserIdHeaderOnDocs(handlerMethod)) {
                operation.addParametersItem(
                        new Parameter().$ref("#/components/parameters/XUserIdHeader")
                );
            }
            return operation;
        };
    }

    /**
     * WebConfig.java를 참고하여
     * swagger-ui.html에서 X-USER-ID 를 보여줄 REST API에 대해 확인합니다.
     * @param handlerMethod
     * @return
     */
    private boolean requiresUserIdHeaderOnDocs(HandlerMethod handlerMethod) {
        Class<?> controllerType = handlerMethod.getBeanType();
        return controllerType == WalletV1Controller.class
                || controllerType == PaymentV1Controller.class;
    }
}
