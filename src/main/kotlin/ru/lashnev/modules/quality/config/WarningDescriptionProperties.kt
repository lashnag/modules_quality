package ru.lashnev.modules.quality.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lashnev.warning-descriptions")
data class WarningDescriptionProperties(
    val acyclicDependencyPrincipleLink: String,
    val stableDependencyPrincipleLink: String,
    val stableAbstractionPrincipleLink: String,
)
