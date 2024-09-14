package ru.lashnev.modules.quality.service

import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import ru.lashnev.modules.quality.config.StableAbstractionProperties
import ru.lashnev.modules.quality.config.WarningDescriptionProperties
import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.model.CodeRepository
import ru.lashnev.modules.quality.model.Connection
import ru.lashnev.modules.quality.model.ModuleInfo
import ru.lashnev.modules.quality.model.RepositoryType
import kotlin.test.assertTrue

@SpringBootTest(classes = [StableAbstractionsPrincipleService::class])
@EnableConfigurationProperties(StableAbstractionProperties::class, WarningDescriptionProperties::class)
class StableAbstractionsPrincipleServiceTest {

    private val maximumRealAbstractnessFactor = 0.5
    private val minimumAbstractnessFactor = 0.0

    @Autowired
    private lateinit var stableAbstractionsPrincipleService: StableAbstractionsPrincipleService

    @MockBean
    private lateinit var moduleInfoService: ModuleInfoDao

    @MockBean
    private lateinit var abstractnessService: AbstractnessService

    @Test
    fun doNotCheckIndependentModule() {
        moduleInfoService.stub {
            on { getModuleByName("module-name") } doReturn ModuleInfo(
                name = "module-name",
                connectionsTo = emptyList(),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository
            )
        }
        val warnings = stableAbstractionsPrincipleService.getWarnings("module-name")
        assertTrue { warnings.isEmpty() }
    }

    @Test
    fun veryAbstractAndStableModuleHasNoError() {
        abstractnessService.stub {
            on { getAbstractnessFactor("module-name") } doReturn maximumRealAbstractnessFactor
        }

        val componentConnectionFrom = Connection("module")
        moduleInfoService.stub {
            on { getModuleByName("module-name") } doReturn ModuleInfo(
                name = "module-name",
                connectionsTo = listOf(Connection("module")),
                connectionsFrom = listOf(
                    componentConnectionFrom, componentConnectionFrom, componentConnectionFrom, componentConnectionFrom,
                    componentConnectionFrom, componentConnectionFrom, componentConnectionFrom, componentConnectionFrom
                ),
                codeRepository = fakeRepository,
            )
        }
        val warnings = stableAbstractionsPrincipleService.getWarnings("module-name")
        assertTrue { warnings.isEmpty() }
    }

    @Test
    fun veryConcreteAndUnstableModuleHasNoError() {
        abstractnessService.stub {
            on { getAbstractnessFactor("module-name") } doReturn minimumAbstractnessFactor
        }
        val componentConnectionsTo = Connection("module")
        moduleInfoService.stub {
            on { getModuleByName("module-name") } doReturn ModuleInfo(
                name = "module-name",
                connectionsTo = listOf(
                    componentConnectionsTo, componentConnectionsTo, componentConnectionsTo, componentConnectionsTo,
                    componentConnectionsTo, componentConnectionsTo, componentConnectionsTo, componentConnectionsTo,
                ),
                connectionsFrom = listOf(Connection("module")),
                codeRepository = fakeRepository,
            )
        }
        val warnings = stableAbstractionsPrincipleService.getWarnings("module-name")
        assertTrue { warnings.isEmpty() }
    }

    @Test
    fun veryConcreteAndStableModuleHasError() {
        abstractnessService.stub {
            on { getAbstractnessFactor("module-name") } doReturn minimumAbstractnessFactor
        }
        val componentConnectionFrom = Connection("module")
        moduleInfoService.stub {
            on { getModuleByName("module-name") } doReturn ModuleInfo(
                name = "module-name",
                connectionsTo = listOf(Connection("module")),
                connectionsFrom = listOf(
                    componentConnectionFrom, componentConnectionFrom, componentConnectionFrom, componentConnectionFrom,
                    componentConnectionFrom, componentConnectionFrom, componentConnectionFrom, componentConnectionFrom
                ),
                codeRepository = fakeRepository
            )
        }
        val warnings = stableAbstractionsPrincipleService.getWarnings("module-name")
        assertTrue { warnings.isNotEmpty() }
    }

    @Test
    fun veryAbstractAndUnstableModuleHasError() {
        abstractnessService.stub {
            on { getAbstractnessFactor("module-name") } doReturn maximumRealAbstractnessFactor
        }
        val componentConnectionsTo = Connection("module")
        moduleInfoService.stub {
            on { getModuleByName("module-name") } doReturn ModuleInfo(
                name = "module-name",
                connectionsTo = listOf(
                    componentConnectionsTo, componentConnectionsTo, componentConnectionsTo, componentConnectionsTo,
                    componentConnectionsTo, componentConnectionsTo, componentConnectionsTo, componentConnectionsTo,
                    componentConnectionsTo, componentConnectionsTo, componentConnectionsTo, componentConnectionsTo
                ),
                connectionsFrom = listOf(Connection("module")),
                codeRepository = fakeRepository,
            )
        }
        val warnings = stableAbstractionsPrincipleService.getWarnings("module-name")
        assertTrue { warnings.isNotEmpty() }
    }

    companion object {
        private val fakeRepository = CodeRepository("", RepositoryType.GERRIT)
    }
}
