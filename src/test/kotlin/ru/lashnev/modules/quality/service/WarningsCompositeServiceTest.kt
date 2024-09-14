package ru.lashnev.modules.quality.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import ru.lashnev.modules.quality.exceptions.NotSupportedModuleException
import ru.lashnev.modules.quality.model.Warning
import ru.lashnev.modules.quality.model.WarningType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(classes = [WarningsCompositeService::class, WarningsService::class])
class WarningsCompositeServiceTest {

    @Autowired
    private lateinit var warningsCompositeService: WarningsCompositeService

    @MockBean
    @Qualifier("firstService")
    private lateinit var warningsServiceFirst: WarningsService
    @MockBean
    @Qualifier("secondService")
    private lateinit var warningsServiceSecond: WarningsService

    @Test
    fun testGetWarningsReturnNoWarnings() {
        assertTrue { warningsCompositeService.getWarnings("module-name").isEmpty() }
    }

    @Test
    fun testGetWarningsReturnWarnings() {
        val warning = Warning(WarningType.ACYCLIC_DEPENDENCY_VIOLATION, "ACYCLIC_DEPENDENCY_VIOLATION", "instruction_url")
        warningsServiceFirst.stub {
            on { getWarnings(Mockito.anyString()) }.doReturn(listOf(warning))
        }
        assertEquals(warningsCompositeService.getWarnings("module-name")[0], warning)
    }

    @Test
    fun testGetWarningsWrongModuleReturnException() {
        warningsServiceFirst.stub {
            on { getWarnings(Mockito.anyString()) }.doThrow(RuntimeException())
        }
        assertThrows<RuntimeException> { warningsCompositeService.getWarnings("module-name") }
    }

    @Test
    fun testGetWarningsEvenIfOnePrincipleNotApplicable() {
        warningsServiceFirst.stub {
            on { getWarnings(Mockito.anyString()) }.doThrow(NotSupportedModuleException("Some description"))
        }
        val warning = Warning(WarningType.STABLE_ABSTRACTION_VIOLATION, "STABLE_ABSTRACTION_VIOLATION", "instruction_url")
        warningsServiceSecond.stub {
            on { getWarnings(Mockito.anyString()) }.doReturn(listOf(warning))
        }
        val warningList = warningsCompositeService.getWarnings("module-name")
        assertEquals(warningList[0], warning)
        assertEquals(warningList.count(), 1)
    }
}
