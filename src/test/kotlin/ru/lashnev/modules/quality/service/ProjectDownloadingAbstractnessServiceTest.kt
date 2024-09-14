package ru.lashnev.modules.quality.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import ru.lashnev.modules.quality.model.LocalRepository
import ru.lashnev.modules.quality.service.analyzers.CodeAnalyzer
import kotlin.io.path.Path
import kotlin.test.assertEquals

@SpringBootTest(classes = [ProjectDownloadingAbstractnessService::class])
class ProjectDownloadingAbstractnessServiceTest {

    @Autowired
    private lateinit var projectDownloadingAbstractnessService: ProjectDownloadingAbstractnessService
    @MockBean
    private lateinit var projectDownloaderService: ProjectDownloaderService
    @MockBean
    @Qualifier("firstAnalyzer")
    private lateinit var firstAnalyzer: CodeAnalyzer
    @MockBean
    @Qualifier("secondAnalyzer")
    private lateinit var secondAnalyzer: CodeAnalyzer

    @BeforeEach
    fun setUp() {
        projectDownloaderService.stub {
            on { downloadProject(any()) } doReturn LocalRepository(Path(""))
        }
    }

    @Test
    fun getHalfAbstractedDoubleLanguageProject() {
        firstAnalyzer.stub {
            on { getClassCount(any()) } doReturn 10
        }
        firstAnalyzer.stub {
            on { getAbstractClassCount(any()) } doReturn 5
        }
        secondAnalyzer.stub {
            on { getClassCount(any()) } doReturn 4
        }
        secondAnalyzer.stub {
            on { getAbstractClassCount(any()) } doReturn 2
        }

        val abstractnessFactor = projectDownloadingAbstractnessService.getAbstractnessFactor("some-project")
        assertEquals(abstractnessFactor, 0.5)
    }

    @Test
    fun dontAnalyzeProjectIfNotSupported() {
        assertThrows<RuntimeException> { projectDownloadingAbstractnessService.getAbstractnessFactor("closure-project") }
    }

    @Test
    fun analyzeProjectIfFirstAnalyzerThrowExceptionOnAbstractClassButSecondAnalyze() {
        firstAnalyzer.stub {
            on { getClassCount(any()) } doReturn 0
        }
        firstAnalyzer.stub {
            on { getAbstractClassCount(any()) } doThrow RuntimeException("Cant analyze some file")
        }
        secondAnalyzer.stub {
            on { getClassCount(any()) } doReturn 8
        }
        secondAnalyzer.stub {
            on { getAbstractClassCount(any()) } doReturn 2
        }
        val abstractnessFactor = projectDownloadingAbstractnessService.getAbstractnessFactor("some-project")
        assertEquals(abstractnessFactor, 0.25)
    }

    @Test
    fun analyzeProjectIfFirstAnalyzerThrowExceptionOnNonAbstractClassButSecondAnalyze() {
        firstAnalyzer.stub {
            on { getClassCount(any()) } doThrow RuntimeException("Cant analyze some file")
        }
        firstAnalyzer.stub {
            on { getAbstractClassCount(any()) } doReturn 0
        }
        secondAnalyzer.stub {
            on { getClassCount(any()) } doReturn 8
        }
        secondAnalyzer.stub {
            on { getAbstractClassCount(any()) } doReturn 1
        }
        val abstractnessFactor = projectDownloadingAbstractnessService.getAbstractnessFactor("some-project")
        assertEquals(abstractnessFactor, 0.125)
    }

    @Test
    fun getAbsoluteConcreteProject() {
        firstAnalyzer.stub {
            on { getClassCount(any()) } doReturn 10
        }
        firstAnalyzer.stub {
            on { getAbstractClassCount(any()) } doReturn 0
        }
        val abstractnessFactor = projectDownloadingAbstractnessService.getAbstractnessFactor("some-project")
        assertEquals(abstractnessFactor, 0.0)
    }

    @Test
    fun getAbsoluteAbstractProject() {
        firstAnalyzer.stub {
            on { getClassCount(any()) } doReturn 10
        }
        firstAnalyzer.stub {
            on { getAbstractClassCount(any()) } doReturn 10
        }
        val abstractnessFactor = projectDownloadingAbstractnessService.getAbstractnessFactor("some-project")
        assertEquals(abstractnessFactor, 1.0)
    }
}
