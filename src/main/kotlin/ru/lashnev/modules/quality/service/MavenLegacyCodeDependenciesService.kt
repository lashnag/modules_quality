package ru.lashnev.modules.quality.service

import org.apache.maven.model.Dependency
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.springframework.stereotype.Service
import ru.lashnev.modules.quality.config.LegacyCodeDependenciesProperties
import ru.lashnev.modules.quality.config.Type
import ru.lashnev.modules.quality.exceptions.NoPomFileException
import ru.lashnev.modules.quality.model.Warning
import ru.lashnev.modules.quality.model.WarningType
import java.io.FileInputStream
import java.io.FileNotFoundException

@Service
class MavenLegacyCodeDependenciesService(
    val legacyCodeDependenciesProperties: LegacyCodeDependenciesProperties,
    val downloaderService: ProjectDownloaderService,
) : WarningsService {

    override fun getWarnings(moduleName: String): List<Warning> {
        val warnings = mutableListOf<Warning>()
        val projectPath = downloaderService.downloadProject(moduleName)
        val reader = MavenXpp3Reader()
        try {
            val fileInputStream = FileInputStream(projectPath.path.toFile().path.plus("/pom.xml"))
            val model: Model = reader.read(fileInputStream)
            setParentWarning(model, warnings)
            setLibrariesWarnings(model, warnings)

            return warnings
        } catch (exception: FileNotFoundException) {
            throw NoPomFileException()
        }
    }

    private fun setParentWarning(model: Model, warnings: MutableList<Warning>) {
        model.parent?.let { pomParent ->
            val legacyParent = legacyCodeDependenciesProperties.dependencies.first { it.type == Type.PARENT }
            if (pomParent.artifactId == legacyParent.name && pomParent.version != null && pomParent.version < legacyParent.minimalVersion) {
                warnings.add(
                    Warning(
                        WarningType.LEGACY_PARENT_DEPENDENCY,
                        "Platform old version ${pomParent.version} < ${legacyParent.minimalVersion}",
                        null
                    )
                )
            }
        }
    }

    private fun setLibrariesWarnings(model: Model, warnings: MutableList<Warning>) {
        legacyCodeDependenciesProperties.dependencies.filter { it.type == Type.LIBRARY }.forEach { legacyDependency ->
            model.dependencies.forEach { pomDependency ->
                if (legacyDependency.name == pomDependency.artifactId && pomDependency.version != null && resolveVersion(pomDependency, model) < legacyDependency.minimalVersion) {
                    warnings.add(
                        Warning(
                            WarningType.LEGACY_CODE_DEPENDENCY,
                            "Dependency old version ${pomDependency.version} < ${legacyDependency.minimalVersion}",
                            null
                        )
                    )
                }
            }
        }
    }

    private fun resolveVersion(dependency: Dependency, model: Model): String {
        return if (dependency.version.startsWith("$")) {
            model.properties.getProperty(dependency.version.removePrefix("\${").removeSuffix("}"), "")
        } else {
            dependency.version
        }
    }
}
