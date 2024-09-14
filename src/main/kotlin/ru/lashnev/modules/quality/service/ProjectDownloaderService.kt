package ru.lashnev.modules.quality.service

import ru.lashnev.modules.quality.model.LocalRepository

interface ProjectDownloaderService {
    fun downloadProject(moduleName: String): LocalRepository
}
