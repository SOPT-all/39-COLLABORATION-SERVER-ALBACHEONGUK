package org.sopt.alba.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import org.sopt.alba.common.auth.CurrentMember;
import org.sopt.alba.common.auth.CurrentMemberArgumentResolver;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    static {
        // @CurrentMember Long memberId 파라미터는 Swagger에서 query 파라미터로 보이지 않게 숨긴다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentMember.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("알바천국 PC 웹 공고 탐색 개선 API")
                        .description("SOPT 39기 합동세미나 웹 2팀 서버 API 명세")
                        .version("v1"))
                // 상대 경로: 로컬이면 localhost, 배포 서버면 https 도메인으로 그대로 호출된다. -> 배포하고 추가하기~
                .addServersItem(new Server().url("/"))
                .components(new Components());
    }

    /** @CurrentMember가 붙은 API에는 X-Member-Id 헤더 입력칸을 자동으로 추가해야 함 */
    @Bean
    public OperationCustomizer memberHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            Arrays.stream(handlerMethod.getMethodParameters())
                    .map(p -> p.getParameterAnnotation(CurrentMember.class))
                    .filter(a -> a != null)
                    .findFirst()
                    .ifPresent(a -> operation.addParametersItem(new HeaderParameter()
                            .name(CurrentMemberArgumentResolver.MEMBER_ID_HEADER)
                            .required(a.required())
                            .description(a.required()
                                    ? "목업 로그인 회원 ID (필수, 예: 1)"
                                    : "목업 로그인 회원 ID (없으면 비로그인)")
                            .schema(new StringSchema().example("1"))));
            return operation;
        };
    }
}