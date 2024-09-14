package ru.lashnev.modules.quality.manual

import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.model.ModuleInfo

open class ModuleInfoWithNewModuleBehavior(private val moduleInfoDao: ModuleInfoDao) : ModuleInfoDao {
    override fun getModuleByName(moduleName: String): ModuleInfo {
        return try {
            moduleInfoDao.getModuleByName(moduleName)
        } catch (e: Exception) {
            ModuleInfo(
                name = moduleName,
                connectionsFrom = emptyList(),
                connectionsTo = emptyList(),
                codeRepository = null
            )
        }
    }
}
