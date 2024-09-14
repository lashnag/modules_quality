package ru.lashnev.modules.quality.manual

import org.springframework.stereotype.Component
import ru.lashnev.modules.quality.config.WarningDescriptionProperties
import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.service.StableDependenciesPrincipleService

@Component
class StableDependencyViolationFixHelper(
    private val moduleInfoDao: ModuleInfoDao,
    private val warningDescriptionProperties: WarningDescriptionProperties
) {
    fun testChangeDependencies(dependencyChange: DependencyChange): Pair<Set<String>, Set<String>> {
        val moduleInfoWithNewModuleBehavior = ModuleInfoWithNewModuleBehavior(moduleInfoDao)
        val moduleInfoWithChangesAndNewModuleBehavior = ModuleInfoDaoWithChanges(moduleInfoWithNewModuleBehavior, dependencyChange)

        val errorsWithoutChanges = getErrors(moduleInfoWithNewModuleBehavior, dependencyChange)
        val errorsWithChanges = getErrors(moduleInfoWithChangesAndNewModuleBehavior, dependencyChange)

        return Pair(errorsWithoutChanges, errorsWithChanges)
    }

    private fun getErrors(moduleInfoDao: ModuleInfoDao, dependencyChange: DependencyChange): MutableSet<String> {
        val stableDependencyService = StableDependenciesPrincipleService(moduleInfoDao, warningDescriptionProperties)
        val errors = mutableSetOf<String>()
        /** Ошибки модуля от которого изменяется связь */
        stableDependencyService.getWarnings(dependencyChange.dependencyChangeFromModuleName).forEach {
            errors.add(it.message)
        }
        /** Ошибки модулей связанных с модулем от которого изменяется связь */
        val dependentFromChangingModules = moduleInfoDao
            .getModuleByName(dependencyChange.dependencyChangeFromModuleName)
            .connectionsFrom
        for (dependentModule in dependentFromChangingModules) {
            stableDependencyService.getWarnings(dependentModule.name).filter {
                it.message.contains(dependencyChange.dependencyChangeFromModuleName)
            }.forEach {
                errors.add(it.message)
            }
        }
        /** Ошибки модуля в который изменяется связь */
        stableDependencyService.getWarnings(dependencyChange.dependencyChangeToModuleName).forEach {
            errors.add(it.message)
        }
        /** Ошибки модулей связанных с модулем в который изменяется связь */
        val dependentToChangingModules = moduleInfoDao
            .getModuleByName(dependencyChange.dependencyChangeToModuleName)
            .connectionsFrom
        for (dependentModule in dependentToChangingModules) {
            stableDependencyService.getWarnings(dependentModule.name).filter {
                it.message.contains(dependencyChange.dependencyChangeToModuleName)
            }.forEach {
                errors.add(it.message)
            }
        }
        return errors
    }
}
