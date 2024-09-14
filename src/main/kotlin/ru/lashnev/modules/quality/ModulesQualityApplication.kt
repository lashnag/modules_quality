package ru.lashnev.modules.quality

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class ModulesQualityApplication

fun main(args: Array<String>) {
    runApplication<ModulesQualityApplication>(*args)
}