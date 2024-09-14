package ru.lashnev.modules.quality.service.analyzers

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.pathString

abstract class JvmFileVisitor : SimpleFileVisitor<Path>() {
    var countClasses = 0

    private val skipDirectories = setOf(
        "/.git",
        "/.svn",
        "/.idea",
        "/.teamcity",
        "/.grafana",
        "/src/test",
        "/src/main/resources",
        "/infra",
        "/db",
        "/target"
    )

    override fun preVisitDirectory(path: Path, attrs: BasicFileAttributes): FileVisitResult {
        return if (skipDirectories.any { path.pathString.endsWith(it) }) {
            FileVisitResult.SKIP_SUBTREE
        } else {
            FileVisitResult.CONTINUE
        }
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        countClasses += getClassCountInFile(file)
        return FileVisitResult.CONTINUE
    }

    abstract fun getClassCountInFile(file: Path): Int
}
