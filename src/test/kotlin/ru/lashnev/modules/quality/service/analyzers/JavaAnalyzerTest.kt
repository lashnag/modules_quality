package ru.lashnev.modules.quality.service.analyzers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@SpringBootTest(classes = [JavaAnalyzer::class])
class JavaAnalyzerTest {

    @Autowired
    private lateinit var javaAnalyzer: JavaAnalyzer

    private val pathToSampleRepos = "src/test/sample/projects/"
    private val pathToKotlinSampleRepo = pathToSampleRepos + "kotlin/"
    private val pathToJavaSampleRepo = pathToSampleRepos + "java/"
    private val pathToJavaSampleRepoMain = pathToJavaSampleRepo + "src/main/"
    private val pathToPythonTestRepo = pathToSampleRepos + "python/"

    @Test
    fun javaAnalyzerLimitOpenedFiles() {
        for (i in 1 until 300) {
            val path = Path(pathToSampleRepos)
            javaAnalyzer.getClassCount(path)
            println(i)
        }
    }

    @Test
    fun javaAnalyzerDontAnalyzeSvnDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, ".svn")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeGitDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, ".svn")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeIdeaDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, ".idea")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeTeamcityDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, ".teamcity")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeGrafanaDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, ".grafana")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeTestsDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, "src/test")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeResourcesDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "resources")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeInfraDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, "infra")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeDbDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, "db")))
    }

    @Test
    fun javaAnalyzerDontAnalyzeTargetDirectory() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepo, "target")))
    }

    @Test
    fun javaAnalyzerSupportKotlinProjectWithJavaFiles() {
        assertNotEquals(0, javaAnalyzer.getClassCount(Path(pathToKotlinSampleRepo)))
    }

    @Test
    fun javaAnalyzerDoesNotSupportOtherProjects() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToPythonTestRepo)))
    }

    @Test
    fun abstractClassAnalyzing() {
        assertEquals(1, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "AbstractClass.java")))
        assertEquals(1, javaAnalyzer.getAbstractClassCount(Path(pathToJavaSampleRepoMain, "AbstractClass.java")))
    }

    @Test
    fun stringsDoesNotDetected() {
        assertEquals(1, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "ClassWithClassWordInString.java")))
        assertEquals(0, javaAnalyzer.getAbstractClassCount(Path(pathToJavaSampleRepoMain, "ClassWithClassWordInString.java")))
    }

    @Test
    fun commentsDoesNotDetected() {
        assertEquals(1, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "ClassWithClassWordInComment.java")))
        assertEquals(0, javaAnalyzer.getAbstractClassCount(Path(pathToJavaSampleRepoMain, "ClassWithClassWordInComment.java")))
    }

    @Test
    fun classWithInnerClassAnalyzing() {
        assertEquals(2, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "ClassWithInnerClass.java")))
        assertEquals(0, javaAnalyzer.getAbstractClassCount(Path(pathToJavaSampleRepoMain, "ClassWithInnerClass.java")))
    }

    @Test
    fun kotlinClassInJavaProjectNotAnalyzed() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "KotlinClass.kt")))
        assertEquals(0, javaAnalyzer.getAbstractClassCount(Path(pathToJavaSampleRepoMain, "KotlinClass.kt")))
    }

    @Test
    fun annotationSupportButDoesNotCounted() {
        assertEquals(0, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "SomeAnnotation.java")))
        assertEquals(0, javaAnalyzer.getAbstractClassCount(Path(pathToJavaSampleRepoMain, "SomeAnnotation.java")))
    }

    @Test
    fun classAnalyzing() {
        assertEquals(1, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "SomeClass.java")))
        assertEquals(0, javaAnalyzer.getAbstractClassCount(Path(pathToJavaSampleRepoMain, "SomeClass.java")))
    }

    @Test
    fun interfaceAnalyzing() {
        assertEquals(1, javaAnalyzer.getClassCount(Path(pathToJavaSampleRepoMain, "SomeInterface.java")))
        assertEquals(1, javaAnalyzer.getAbstractClassCount(Path(pathToJavaSampleRepoMain, "SomeInterface.java")))
    }
}
