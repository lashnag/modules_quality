package ru.lashnev.modules.quality.service

import org.springframework.stereotype.Component
import ru.lashnev.modules.quality.config.StableAbstractionProperties
import ru.lashnev.modules.quality.config.WarningDescriptionProperties
import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.model.DependenciesCount
import ru.lashnev.modules.quality.model.Warning
import ru.lashnev.modules.quality.model.WarningType
import kotlin.math.abs

@Component
class StableAbstractionsPrincipleService(
    private val moduleInfoService: ModuleInfoDao,
    private val abstractnessService: AbstractnessService,
    private val warningDescriptionProperties: WarningDescriptionProperties,
    configProperties: StableAbstractionProperties,
) : WarningsService {

    private val maxDistanceFromOptimalDiagonal = configProperties.maxDistanceFromOptimalDiagonal

    override fun getWarnings(moduleName: String): List<Warning> {
        val abstractnessFactor = abstractnessService.getAbstractnessFactor(moduleName)
        val instabilityFactor = getInstabilityFactor(moduleName)
        val distanceFromDiagonal = abs(abstractnessFactor + instabilityFactor - 1)
        return if (instabilityFactor != 0.0 && distanceFromDiagonal > maxDistanceFromOptimalDiagonal) {
            listOf(
                Warning(
                    type = WarningType.STABLE_ABSTRACTION_VIOLATION,
                    message = "Компонент находится слишком далеко ${String.format("%.3f", distanceFromDiagonal)} >" +
                        " $maxDistanceFromOptimalDiagonal от оптимальной диагонали I/A",
                    descriptionLink = warningDescriptionProperties.stableAbstractionPrincipleLink,
                )
            )
        } else {
            emptyList()
        }
    }

    private fun getInstabilityFactor(moduleName: String): Double {
        val dependenciesCount = getDependenciesCount(moduleName)
        return if (dependenciesCount.directionIn + dependenciesCount.directionOut == 0) {
            0.0
        } else {
            dependenciesCount.directionOut.toDouble() / (dependenciesCount.directionIn + dependenciesCount.directionOut)
        }
    }

    private fun getDependenciesCount(moduleName: String): DependenciesCount {
        val moduleInfo = moduleInfoService.getModuleByName(moduleName)
        return DependenciesCount(moduleInfo.connectionsFrom.count(), moduleInfo.connectionsTo.count())
    }
}
