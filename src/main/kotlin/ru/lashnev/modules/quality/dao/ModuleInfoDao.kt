package ru.lashnev.modules.quality.dao

import ru.lashnev.modules.quality.model.ModuleInfo

interface ModuleInfoDao {
    fun getModuleByName(moduleName: String): ModuleInfo
}
