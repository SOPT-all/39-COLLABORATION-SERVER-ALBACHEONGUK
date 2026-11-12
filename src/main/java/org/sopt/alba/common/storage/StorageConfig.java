package org.sopt.alba.common.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SupabaseStorageProperties.class)
public class StorageConfig {
}
