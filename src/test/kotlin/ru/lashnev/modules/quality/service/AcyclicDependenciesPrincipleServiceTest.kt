package ru.lashnev.modules.quality.service

import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import ru.lashnev.modules.quality.config.WarningDescriptionProperties
import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.model.CodeRepository
import ru.lashnev.modules.quality.model.Connection
import ru.lashnev.modules.quality.model.ModuleInfo
import ru.lashnev.modules.quality.model.RepositoryType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(classes = [AcyclicDependenciesPrincipleService::class])
@EnableConfigurationProperties(WarningDescriptionProperties::class)
class AcyclicDependenciesPrincipleServiceTest {

    @Autowired
    private lateinit var acyclicDependenciesPrincipleService: AcyclicDependenciesPrincipleService

    @MockBean
    private lateinit var moduleInfoService: ModuleInfoDao

    @Test
    fun noWarningIfModuleHasNoAcyclicDependency() {
        moduleInfoService.stub {
            on { getModuleByName("module-name") } doReturn ModuleInfo(
                name = "module-name",
                connectionsTo = listOf(Connection("module-1")),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository,
            )
        }
        moduleInfoService.stub {
            on { getModuleByName("module-1") } doReturn ModuleInfo(
                name = "module-1",
                connectionsTo = emptyList(),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository,
            )
        }
        val warnings = acyclicDependenciesPrincipleService.getWarnings("module-name")
        assertTrue { warnings.isEmpty() }
    }

    @Test
    fun warningIfModuleHasAcyclicDependency() {
        moduleInfoService.stub {
            on { getModuleByName("module-name") } doReturn ModuleInfo(
                name = "module-name",
                connectionsTo = listOf(
                    Connection("module-1"),
                    Connection("module-2"),
                ),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository,
            )
        }
        moduleInfoService.stub {
            on { getModuleByName("module-1") } doReturn ModuleInfo(
                name = "module-1",
                connectionsTo = listOf(Connection("module-name")),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository,
            )
        }
        moduleInfoService.stub {
            on { getModuleByName("module-2") } doReturn ModuleInfo(
                name = "module-2",
                connectionsTo = listOf(Connection("module-3")),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository
            )
        }
        moduleInfoService.stub {
            on { getModuleByName("module-3") } doReturn ModuleInfo(
                name = "module-3",
                connectionsTo = listOf(Connection("module-name")),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository,
            )
        }
        val warnings = acyclicDependenciesPrincipleService.getWarnings("module-name")
        assertEquals(2, warnings.count())
    }

    companion object {
        private val fakeRepository = CodeRepository("", RepositoryType.GERRIT)
    }
}
