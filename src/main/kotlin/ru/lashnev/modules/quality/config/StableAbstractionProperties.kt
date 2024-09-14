package ru.lashnev.modules.quality.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lashnev.quality-limits.stable-abstraction")
data class StableAbstractionProperties(val maxDistanceFromOptimalDiagonal: Double)
