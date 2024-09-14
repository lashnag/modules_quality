package ru.lashnev.modules.quality.dao

import org.springframework.stereotype.Repository
import ru.lashnev.modules.quality.model.Connection
import ru.lashnev.modules.quality.model.ModuleInfo

@Repository
class SampleModuleInfoRepositoryDao : ModuleInfoDao {

    override fun getModuleByName(moduleName: String): ModuleInfo {
        return ModuleInfo(
            name = moduleName,
            connectionsTo = emptyList(),
            connectionsFrom = emptyList(),
            codeRepository = null
        )
    }
}
