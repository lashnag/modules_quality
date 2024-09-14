package ru.lashnev.modules.quality.service.analyzers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@SpringBootTest(classes = [KotlinAnalyzer::class])
class KotlinAnalyzerTest {

    @Autowired
    private lateinit var kotlinAnalyzer: KotlinAnalyzer

    private val pathToSampleRepos = "src/test/sample/projects/"
    private val pathToKotlinSampleRepo = pathToSampleRepos + "kotlin/"
    private val pathToKotlinSampleRepoMain = pathToKotlinSampleRepo + "src/main/"
    private val pathToJavaSampleRepo = pathToSampleRepos + "java/"
    private val pathToPythonTestRepo = pathToSampleRepos + "python/"

    @Test
    fun kotlinAnalyzerLimitOpenedFiles() {
        for (i in 1 until 300) {
            val path = Path(pathToSampleRepos)
            kotlinAnalyzer.getClassCount(path)
            println(i)
        }
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeSvnDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, ".svn")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeGitDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, ".git")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeIdeaDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, ".idea")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeTeamcityDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, ".teamcity")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeGrafanaDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, ".grafana")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeTestsDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, "src/test")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeResourcesDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "resources")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeInfraDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, "infra")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeDbDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, "db")))
    }

    @Test
    fun kotlinAnalyzerDontAnalyzeTargetDirectory() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepo, "target")))
    }

    @Test
    fun kotlinAnalyzerSupportJavaProjectWithKotlinFiles() {
        assertNotEquals(0, kotlinAnalyzer.getClassCount(Path(pathToJavaSampleRepo)))
    }

    @Test
    fun kotlinAnalyzerDoesNotSupportOtherProjects() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToPythonTestRepo)))
    }

    @Test
    fun abstractClassAnalyzed() {
        assertEquals(1, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "AbstractClass.kt")))
        assertEquals(1, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "AbstractClass.kt")))
    }

    @Test
    fun stringsDoesNotDetected() {
        assertEquals(1, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "ClassWithClassWordInString.kt")))
        assertEquals(0, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "ClassWithClassWordInString.kt")))
    }

    @Test
    fun commentsDoesNotDetected() {
        assertEquals(1, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "ClassWithClassWordInComment.kt")))
        assertEquals(0, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "ClassWithClassWordInComment.kt")))
    }

    @Test
    fun fileWithFewClassesAnalyzed() {
        assertEquals(2, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "FileWithFewClasses.kt")))
        assertEquals(0, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "FileWithFewClasses.kt")))
    }

    @Test
    fun javaClassInKotlinProjectNotAnalyzing() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "JavaClass.java")))
        assertEquals(0, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "JavaClass.java")))
    }

    @Test
    fun sealedClassAnalyzedWithChild() {
        assertEquals(4, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "SealedClass.kt")))
        assertEquals(0, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "SealedClass.kt")))
    }

    @Test
    fun annotationDoesNotCounted() {
        assertEquals(0, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "SomeAnnotation.kt")))
        assertEquals(0, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "SomeAnnotation.kt")))
    }

    @Test
    fun interfaceAnalyzed() {
        assertEquals(1, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "SomeInterface.kt")))
        assertEquals(1, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "SomeInterface.kt")))
    }

    @Test
    fun objectAnalyzed() {
        assertEquals(1, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "SomeObject.kt")))
        assertEquals(0, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "SomeObject.kt")))
    }

    @Test
    fun classAnalyzed() {
        assertEquals(1, kotlinAnalyzer.getClassCount(Path(pathToKotlinSampleRepoMain, "SomeClass.kt")))
        assertEquals(0, kotlinAnalyzer.getAbstractClassCount(Path(pathToKotlinSampleRepoMain, "SomeClass.kt")))
    }
}
