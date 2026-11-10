package org.sopt.alba.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * "오늘"의 기준을 KST로 고정한다. (마감 여부, n일 전 라벨 계산)
 * 서비스에서는 LocalDate.now() 대신 LocalDate.now(clock)을 쓴다. > 테스트에서 날짜 고정 가능
 */

@Configuration
public class ClockConfig {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(KST);
    }
}