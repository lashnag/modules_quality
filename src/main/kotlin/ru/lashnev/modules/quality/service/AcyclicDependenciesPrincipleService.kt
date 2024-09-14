package ru.lashnev.modules.quality.service

import org.springframework.stereotype.Service
import ru.lashnev.modules.quality.config.WarningDescriptionProperties
import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.model.Warning
import ru.lashnev.modules.quality.model.WarningType
import java.util.ArrayDeque

@Service
class AcyclicDependenciesPrincipleService(
    private val moduleInfoService: ModuleInfoDao,
    private val warningDescriptionProperties: WarningDescriptionProperties,
) : WarningsService {

    override fun getWarnings(moduleName: String): List<Warning> {
        val visitedServices = mutableSetOf<String>()
        val dependencyErrors = mutableListOf<Warning>()
        val visitedServicesStack = ArrayDeque<Pair<String, String>>()
        visitedServicesStack.addLast(moduleName to moduleName)
        while (visitedServicesStack.isNotEmpty()) {
            val (serviceName, path) = visitedServicesStack.removeLast()
            if (!visitedServices.add(serviceName)) continue

            for (connection in moduleInfoService.getModuleByName(serviceName).connectionsTo) {
                if (connection.name == moduleName && serviceName != moduleName) {
                    dependencyErrors.add(
                        Warning(
                            type = WarningType.ACYCLIC_DEPENDENCY_VIOLATION,
                            message = "Найдена циклическая зависимость: $path.$moduleName",
                            descriptionLink = warningDescriptionProperties.acyclicDependencyPrincipleLink,
                        )
                    )
                }
                visitedServicesStack.addLast(connection.name to "$path.${connection.name}")
            }
        }

        return dependencyErrors
    }
}
