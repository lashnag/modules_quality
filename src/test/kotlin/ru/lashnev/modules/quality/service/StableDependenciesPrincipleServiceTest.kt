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
import kotlin.test.assertTrue

@SpringBootTest(classes = [StableDependenciesPrincipleService::class])
@EnableConfigurationProperties(WarningDescriptionProperties::class)
class StableDependenciesPrincipleServiceTest {

    @Autowired
    private lateinit var stableDependenciesPrincipleService: StableDependenciesPrincipleService

    @MockBean
    private lateinit var moduleInfoService: ModuleInfoDao

    @Test
    fun noWarningIfModuleHaveTheSameStability() {
        moduleInfoService.stub {
            on { getModuleByName("module-name-1") } doReturn ModuleInfo(
                name = "module-name-1",
                connectionsTo = listOf(Connection("module-name-2")),
                connectionsFrom = listOf(Connection("module-name-0")),
                codeRepository = fakeRepository,
            )
        }
        moduleInfoService.stub {
            on { getModuleByName("module-name-2") } doReturn ModuleInfo(
                name = "module-name-2",
                connectionsTo = listOf(Connection("module-name-3")),
                connectionsFrom = listOf(Connection("module-name-1")),
                codeRepository = fakeRepository,
            )
        }
        val warnings = stableDependenciesPrincipleService.getWarnings("module-name-1")
        assertTrue { warnings.isEmpty() }
    }

    @Test
    fun noWarningIfModuleHasNoDependentModules() {
        moduleInfoService.stub {
            on { getModuleByName("module-name") } doReturn ModuleInfo(
                name = "module-name",
                connectionsTo = emptyList(),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository,
            )
        }
        val warnings = stableDependenciesPrincipleService.getWarnings("module-name")
        assertTrue { warnings.isEmpty() }
    }

    @Test
    fun getWarningIfMoreStableModuleDependsOfLessStableModule() {
        moduleInfoService.stub {
            on { getModuleByName("2-ground-component") } doReturn ModuleInfo(
                name = "2-ground-component",
                connectionsTo = listOf(Connection("1-ground-component")),
                connectionsFrom = listOf(
                    Connection("3-ground-component-1"),
                    Connection("3-ground-component-2"),
                ),
                codeRepository = fakeRepository,
            )
        }

        moduleInfoService.stub {
            on { getModuleByName("1-ground-component") } doReturn ModuleInfo(
                name = "1-ground-component",
                connectionsTo = listOf(
                    Connection("0-ground-component-1"),
                    Connection("0-ground-component-2"),
                ),
                connectionsFrom = listOf(Connection("2-ground-component")),
                codeRepository = fakeRepository,
            )
        }

        val warnings = stableDependenciesPrincipleService.getWarnings("2-ground-component")
        assertTrue { warnings.isNotEmpty() }
    }

    @Test
    fun noWarningIfLessStableModuleDependsOnMoreStableModule() {
        moduleInfoService.stub {
            on { getModuleByName("2-ground-component") } doReturn ModuleInfo(
                name = "2-ground-component",
                connectionsTo = listOf(Connection("1-ground-component")),
                connectionsFrom = emptyList(),
                codeRepository = fakeRepository,
            )
        }

        moduleInfoService.stub {
            on { getModuleByName("1-ground-component") } doReturn ModuleInfo(
                name = "1-ground-component",
                connectionsTo = listOf(
                    Connection("0-ground-component-1"),
                    Connection("0-ground-component-2"),
                ),
                connectionsFrom = listOf(Connection("2-ground-component")),
                codeRepository = fakeRepository,
            )
        }
        val warnings = stableDependenciesPrincipleService.getWarnings("2-ground-component")
        assertTrue { warnings.isEmpty() }
    }

    companion object {
        private val fakeRepository = CodeRepository("", RepositoryType.GERRIT)
    }
}
