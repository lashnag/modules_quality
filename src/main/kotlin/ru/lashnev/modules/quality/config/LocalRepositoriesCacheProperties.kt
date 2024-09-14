package ru.lashnev.modules.quality.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "lashnev.cache.local-repositories")
data class LocalRepositoriesCacheProperties(val ttl: Duration, val maximumElements: Long)
