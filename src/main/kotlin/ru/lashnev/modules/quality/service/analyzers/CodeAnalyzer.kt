package ru.lashnev.modules.quality.service.analyzers

import java.nio.file.Path

interface CodeAnalyzer {
    fun getClassCount(modulePath: Path): Int
    fun getAbstractClassCount(modulePath: Path): Int
}
