package ru.lashnev.modules.quality.service

import org.springframework.stereotype.Component
import ru.lashnev.modules.quality.exceptions.NotSupportedModuleException
import ru.lashnev.modules.quality.model.Warning
import ru.lashnev.modules.quality.utils.logger

@Component
class WarningsCompositeService(private val warningsServices: List<WarningsService>) {

    companion object {
        private val logger = logger()
    }

    fun getWarnings(moduleName: String): List<Warning> {
        val warnings = mutableListOf<Warning>()
        for (warningsService in warningsServices) {
            try {
                warnings.addAll(warningsService.getWarnings(moduleName))
            } catch (e: NotSupportedModuleException) {
                logger.warn("Cant evaluate module $moduleName by service ${warningsService::class.java}", e)
            }
        }
        return warnings
    }
}
