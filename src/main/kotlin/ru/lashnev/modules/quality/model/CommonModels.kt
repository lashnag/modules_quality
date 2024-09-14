package ru.lashnev.modules.quality.model

import org.apache.tomcat.util.http.fileupload.FileUtils
import java.nio.file.Path

data class ModuleInfo(
    val name: String,
    val connectionsTo: List<Connection>,
    val connectionsFrom: List<Connection>,
    val codeRepository: CodeRepository?,
)

data class Warning(val type: WarningType, val message: String, val descriptionLink: String?)

enum class WarningType {
    ACYCLIC_DEPENDENCY_VIOLATION,
    STABLE_ABSTRACTION_VIOLATION,
    STABLE_DEPENDENCY_VIOLATION,
    LEGACY_PARENT_DEPENDENCY,
    LEGACY_CODE_DEPENDENCY,
}

data class LocalRepository(val path: Path) : AutoCloseable {
    override fun close() {
        FileUtils.deleteDirectory(path.toFile())
    }
}

data class DependenciesCount(val directionIn: Int, val directionOut: Int)
data class Connection(val name: String)
data class CodeRepository(val link: String, val type: RepositoryType)
enum class RepositoryType {
    GERRIT,
    GITHUB
}
