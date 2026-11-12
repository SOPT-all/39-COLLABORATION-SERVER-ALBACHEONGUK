package org.sopt.alba.common.storage;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "supabase.storage")
public class SupabaseStorageProperties {
    private boolean enabled;
    private URI url;
    private String secretKey;
    private String bucket = "job-images";
    @Min(60)
    @Max(3600)
    private int signedUrlTtlSeconds = 600;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);

    @AssertTrue(message = "Storage 활성화 시 HTTPS 프로젝트 URL, 서버 전용 키, 버킷을 설정해야 합니다.")
    public boolean isConfigurationValid() {
        if (!enabled) {
            return true;
        }
        if (url == null || url.getHost() == null || url.getRawQuery() != null
                || url.getRawFragment() != null || url.getUserInfo() != null
                || (url.getPath() != null && !url.getPath().isEmpty() && !"/".equals(url.getPath()))) {
            return false;
        }
        boolean secure = "https".equals(url.getScheme())
                || ("http".equals(url.getScheme()) && "127.0.0.1".equals(url.getHost()));
        boolean serverKey = secretKey != null
                && (secretKey.startsWith("sb_secret_") || secretKey.startsWith("eyJ"));
        return secure && serverKey && bucket != null && bucket.matches("[a-z0-9][a-z0-9-]{0,62}")
                && connectTimeout != null && !connectTimeout.isZero() && !connectTimeout.isNegative()
                && readTimeout != null && !readTimeout.isZero() && !readTimeout.isNegative();
    }
}
