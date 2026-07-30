package io.github.artsobol.mediaservice.config.retryable;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.retryable-task")
public record RetryableTaskProperties(Integer limit, Duration retryDelay) {}
