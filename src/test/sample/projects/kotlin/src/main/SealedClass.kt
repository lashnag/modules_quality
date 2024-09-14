package ru.lashnev.modules.quality.sample.projects.kotlin.main.src

sealed class SealedClass {
    object FirstClass : SealedClass()
    object SecondClass : SealedClass()
    class ThirdClass(val str: String) : SealedClass()
}
