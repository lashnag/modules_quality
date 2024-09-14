package ru.lashnev.modules.quality.service

import ru.lashnev.modules.quality.model.Warning

interface WarningsService {
    fun getWarnings(moduleName: String): List<Warning>
}
