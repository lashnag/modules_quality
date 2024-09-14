package ru.lashnev.modules.quality.service

import org.springframework.stereotype.Service
import ru.lashnev.modules.quality.config.WarningDescriptionProperties
import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.model.DependenciesCount
import ru.lashnev.modules.quality.model.Warning
import ru.lashnev.modules.quality.model.WarningType
import java.util.Optional

@Service
class StableDependenciesPrincipleService(
    private val moduleInfoService: ModuleInfoDao,
    private val warningDescriptionProperties: WarningDescriptionProperties,
) : WarningsService {

    override fun getWarnings(moduleName: String): List<Warning> {
        val errors = mutableSetOf<Warning>()
        val analyzeComponentInstabilityFactor = getInstabilityFactor(moduleName)
        for (dependentToComponent in getDependenciesTo(moduleName)) {
            val error = findStabilityError(dependentToComponent, moduleName, analyzeComponentInstabilityFactor)
            error.map { errors.add(it) }
        }
        return errors.toList()
    }

    private fun findStabilityError(
        dependentToComponent: String,
        analyzedComponent: String,
        analyzeComponentInstabilityFactor: Double
    ): Optional<Warning> {
        val dependentToComponentInstabilityFactor = getInstabilityFactor(dependentToComponent)
        return if (dependentToComponentInstabilityFactor > analyzeComponentInstabilityFactor) {
            Optional.of(
                Warning(
                    type = WarningType.STABLE_DEPENDENCY_VIOLATION,
                    message = "Более стабильный модуль $analyzedComponent (I =" +
                        " ${String.format("%.3f", analyzeComponentInstabilityFactor)}) зависит от менее стабильного " +
                        "$dependentToComponent (I = ${String.format("%.3f", dependentToComponentInstabilityFactor)})",
                    descriptionLink = warningDescriptionProperties.stableDependencyPrincipleLink,
                )
            )
        } else {
            Optional.empty()
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

    private fun getDependenciesTo(moduleName: String): Set<String> {
        return moduleInfoService.getModuleByName(moduleName).connectionsTo.map { it.name }.toSet()
    }

    private fun getDependenciesCount(moduleName: String): DependenciesCount {
        val moduleInfo = moduleInfoService.getModuleByName(moduleName)
        return DependenciesCount(moduleInfo.connectionsFrom.count(), moduleInfo.connectionsTo.count())
    }
}
