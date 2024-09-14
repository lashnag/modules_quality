package ru.lashnev.modules.quality.service

import org.springframework.stereotype.Service
import ru.lashnev.modules.quality.exceptions.NotSupportedLanguageException
import ru.lashnev.modules.quality.service.analyzers.CodeAnalyzer
import ru.lashnev.modules.quality.utils.logger

@Service
class ProjectDownloadingAbstractnessService(
    private val projectDownloaderService: ProjectDownloaderService,
    private val analyzers: List<CodeAnalyzer>
) : AbstractnessService {

    override fun getAbstractnessFactor(moduleName: String): Double {
        projectDownloaderService.downloadProject(moduleName).use { localRepository ->
            val allClassesCount = analyzers.sumOf {
                try {
                    it.getClassCount(localRepository.path)
                } catch (exception: RuntimeException) {
                    logger.warn("Exception in getClassCount on analyzer ${it.javaClass} and module $moduleName", exception)
                    0
                }
            }
            val abstractClassesCount = analyzers.sumOf {
                try {
                    it.getAbstractClassCount(localRepository.path)
                } catch (exception: Exception) {
                    logger.warn("Exception in getAbstractClassCount on analyzer ${it.javaClass} and module $moduleName", exception)
                    0
                }
            }
            if (allClassesCount == 0) {
                throw NotSupportedLanguageException()
            }

            return abstractClassesCount.toDouble() / allClassesCount
        }
    }

    companion object {
        val logger = logger()
    }
}
