package ru.lashnev.modules.quality.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lashnev.legacy-code")
data class LegacyCodeDependenciesProperties(val dependencies: List<Dependency>)
data class Dependency(val type: Type, val name: String, val minimalVersion: String)
enum class Type {
    PARENT, LIBRARY
}
