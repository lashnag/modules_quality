package ru.lashnev.modules.quality.manual

import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.model.Connection
import ru.lashnev.modules.quality.model.ModuleInfo

class ModuleInfoDaoWithChanges(
    private val moduleInfoDao: ModuleInfoDao,
    private val dependencyChange: DependencyChange
) : ModuleInfoWithNewModuleBehavior(moduleInfoDao) {
    override fun getModuleByName(moduleName: String): ModuleInfo {
        val moduleInfo = moduleInfoDao.getModuleByName(moduleName)

        if (moduleName == dependencyChange.dependencyChangeFromModuleName) {
            return when (dependencyChange.changingType) {
                ChangingType.ADD -> moduleInfo.copy(connectionsTo = moduleInfo.connectionsTo.plus(Connection(dependencyChange.dependencyChangeToModuleName)))
                ChangingType.DELETE -> moduleInfo.copy(connectionsTo = moduleInfo.connectionsTo.filterNot { it.name == dependencyChange.dependencyChangeToModuleName })
            }
        }
        if (moduleName == dependencyChange.dependencyChangeToModuleName) {
            return when (dependencyChange.changingType) {
                ChangingType.ADD -> moduleInfo.copy(connectionsFrom = moduleInfo.connectionsFrom.plus(Connection(dependencyChange.dependencyChangeFromModuleName)))
                ChangingType.DELETE -> moduleInfo.copy(connectionsFrom = moduleInfo.connectionsFrom.filterNot { it.name == dependencyChange.dependencyChangeFromModuleName })
            }
        }

        return moduleInfo
    }
}
