package ru.lashnev.modules.quality.manual

data class DependencyChange(
    val dependencyChangeFromModuleName: String,
    val dependencyChangeToModuleName: String,
    val changingType: ChangingType
)
