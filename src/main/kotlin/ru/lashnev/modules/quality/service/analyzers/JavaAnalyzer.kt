package ru.lashnev.modules.quality.service.analyzers

import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.lang.jvm.JvmClassKind
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtilRt
import org.jetbrains.kotlin.com.intellij.psi.PsiClass
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.com.intellij.psi.PsiModifier
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText

@Service
class JavaAnalyzer : CodeAnalyzer {

    private val javaFileExtension = "java"

    override fun getClassCount(modulePath: Path): Int {
        val fileVisitor = object : JvmFileVisitor() {
            override fun getClassCountInFile(file: Path): Int {
                if (file.extension != javaFileExtension) {
                    return 0
                }
                var count = 0
                val psiJavaFile = createJavaFile(file)
                psiJavaFile?.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (isClass(element)) {
                            count++
                        }
                        super.visitElement(element)
                    }

                    private fun isClass(element: PsiElement) =
                        element is PsiClass && element.classKind != JvmClassKind.ANNOTATION
                })
                return count
            }
        }

        Files.walkFileTree(modulePath, fileVisitor)
        return fileVisitor.countClasses
    }

    override fun getAbstractClassCount(modulePath: Path): Int {
        val fileVisitor = object : JvmFileVisitor() {
            override fun getClassCountInFile(file: Path): Int {
                if (file.extension != javaFileExtension) {
                    return 0
                }
                var count = 0
                val psiJavaFile = createJavaFile(file)
                psiJavaFile?.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (isAbstract(element)) {
                            count++
                        }
                        super.visitElement(element)
                    }

                    private fun isAbstract(element: PsiElement) = element is PsiClass &&
                        element.classKind != JvmClassKind.ANNOTATION &&
                        element.modifierList?.hasModifierProperty(PsiModifier.ABSTRACT) == true
                })
                return count
            }
        }

        Files.walkFileTree(modulePath, fileVisitor)
        return fileVisitor.countClasses
    }

    private fun createJavaFile(path: Path): PsiJavaFile? {
        val eventSystemEnabled = true
        val markAsCopy = true
        val noSizeLimit = false
        return createPsiFactoryInstance().createFileFromText(
            path.fileName.toString(),
            JavaLanguage.INSTANCE,
            StringUtilRt.convertLineSeparators(path.readText()),
            eventSystemEnabled,
            markAsCopy,
            noSizeLimit,
            LightVirtualFile(path.toString())
        ) as PsiJavaFile?
    }
}
