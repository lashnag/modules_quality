package ru.lashnev.modules.quality.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lashnev.git.credentials")
data class GitProperties(
    val base64PrivateKey: String,
    val gerritLogin: String,
    val gerritPort: String,
    val githubPort: String,
)
