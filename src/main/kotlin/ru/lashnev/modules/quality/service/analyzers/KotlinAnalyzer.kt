package ru.lashnev.modules.quality.service.analyzers

import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtilRt
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText

@Service
class KotlinAnalyzer : CodeAnalyzer {

    private val kotlinFileExtension = "kt"

    override fun getClassCount(modulePath: Path): Int {
        val fileVisitor = object : JvmFileVisitor() {
            override fun getClassCountInFile(file: Path): Int {
                if (file.extension != kotlinFileExtension) {
                    return 0
                }
                var count = 0
                val psiKtFile = createKtFile(file)
                psiKtFile?.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is KtClass && !element.isAnnotation()) {
                            count++
                        }
                        if (element is KtObjectDeclaration) {
                            count++
                        }
                        super.visitElement(element)
                    }
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
                if (file.extension != kotlinFileExtension) {
                    return 0
                }
                var count = 0
                val psiKtFileFile = createKtFile(file)
                psiKtFileFile?.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is KtClass && element.isAbstract() && !element.isAnnotation()) {
                            count++
                        }
                        super.visitElement(element)
                    }
                })
                return count
            }
        }

        Files.walkFileTree(modulePath, fileVisitor)
        return fileVisitor.countClasses
    }

    private fun createKtFile(path: Path): KtFile? {
        val eventSystemEnabled = true
        val markAsCopy = true
        val noSizeLimit = false
        return createPsiFactoryInstance().createFileFromText(
            path.fileName.toString(),
            KotlinLanguage.INSTANCE,
            StringUtilRt.convertLineSeparators(path.readText()),
            eventSystemEnabled,
            markAsCopy,
            noSizeLimit,
            LightVirtualFile(path.toString())
        ) as KtFile?
    }
}
