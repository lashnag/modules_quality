package ru.lashnev.modules.quality.service

interface AbstractnessService {
    fun getAbstractnessFactor(moduleName: String): Double
}
