package org.sopt.alba.common.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import org.sopt.alba.common.exception.BusinessException;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SupabaseStorageService {
    private final SupabaseStorageProperties properties;
    private final Clock clock;
    private final RestClient client;

    public SupabaseStorageService(RestClient.Builder builder, SupabaseStorageProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout()).build());
        factory.setReadTimeout(properties.getReadTimeout());
        RestClient.Builder configured = builder.clone().requestFactory(factory);
        if (properties.isEnabled()) {
            configured.defaultHeader("apikey", properties.getSecretKey());
            // New secret keys belong in apikey; legacy service_role keys are JWTs.
            if (!properties.getSecretKey().startsWith("sb_secret_")) {
                configured.defaultHeader("Authorization", "Bearer " + properties.getSecretKey());
            }
        }
        this.client = configured.build();
    }

    public SignedImageUrl createSignedUrl(String objectPath) {
        if (!properties.isEnabled()) {
            throw new BusinessException(StorageErrorCode.STORAGE_NOT_CONFIGURED);
        }
        if (objectPath == null || !objectPath.matches("[A-Za-z0-9][A-Za-z0-9._/-]{0,499}")
                || Arrays.stream(objectPath.split("/", -1))
                .anyMatch(segment -> segment.isEmpty() || segment.equals(".") || segment.equals(".."))) {
            throw new BusinessException(StorageErrorCode.INVALID_IMAGE_PATH);
        }
        URI endpoint = UriComponentsBuilder.fromUri(properties.getUrl())
                .replacePath(null)
                .pathSegment("storage", "v1", "object", "sign", properties.getBucket())
                .pathSegment(objectPath.split("/"))
                .build().encode().toUri();
        Instant expiresAt = clock.instant().plusSeconds(properties.getSignedUrlTtlSeconds());
        try {
            SignResponse response = client.post().uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", properties.getSignedUrlTtlSeconds()))
                    .retrieve().body(SignResponse.class);
            if (response == null || response.signedUrl() == null
                    || !response.signedUrl().startsWith("/object/sign/")
                    || response.signedUrl().contains("#")) {
                throw new BusinessException(StorageErrorCode.STORAGE_UNAVAILABLE);
            }
            String origin = UriComponentsBuilder.fromUri(properties.getUrl()).replacePath(null)
                    .build().toUriString();
            return new SignedImageUrl(origin + "/storage/v1" + response.signedUrl(), expiresAt);
        } catch (RestClientException e) {
            // Provider bodies/headers can contain credentials or tokens; do not log them.
            throw new BusinessException(StorageErrorCode.STORAGE_UNAVAILABLE);
        }
    }

    private record SignResponse(@JsonProperty("signedURL") String signedUrl) {
    }
}
