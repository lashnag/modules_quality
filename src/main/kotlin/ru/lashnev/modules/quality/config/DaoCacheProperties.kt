package ru.lashnev.modules.quality.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "lashnev.cache.dao")
data class DaoCacheProperties(val ttl: Duration, val maximumElements: Long)
