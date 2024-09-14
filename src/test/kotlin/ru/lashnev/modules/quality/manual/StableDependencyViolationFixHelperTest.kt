package ru.lashnev.modules.quality.manual

import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import ru.lashnev.modules.quality.config.WarningDescriptionProperties
import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.model.Connection
import ru.lashnev.modules.quality.model.ModuleInfo
import ru.lashnev.modules.quality.service.StableDependenciesPrincipleService
import kotlin.test.assertEquals

@EnableConfigurationProperties(WarningDescriptionProperties::class)
@SpringBootTest(classes = [StableDependencyViolationFixHelper::class, StableDependenciesPrincipleService::class])
class StableDependencyViolationFixHelperTest {

    @Autowired
    private lateinit var stableDependencyViolationFixHelper: StableDependencyViolationFixHelper

    @MockBean
    private lateinit var moduleInfoDao: ModuleInfoDao

    @Test
    fun testIndependentModuleAddNewModuleDependentHasNoErrorsBeforeAndAfterChange() {
        moduleInfoDao.stub {
            on { getModuleByName("independent-before-change-module") } doReturn ModuleInfo(
                name = "independent-before-change-module",
                connectionsTo = emptyList(),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("new-module-name") } doThrow IllegalStateException("Module not found")
        }

        val errorsBeforeAndAfterChange = stableDependencyViolationFixHelper.testChangeDependencies(
            DependencyChange(
                dependencyChangeFromModuleName = "independent-before-change-module",
                dependencyChangeToModuleName = "new-module-name",
                changingType = ChangingType.ADD
            )
        )

        assertEquals(0, errorsBeforeAndAfterChange.first.size)
        assertEquals(0, errorsBeforeAndAfterChange.second.size)
    }

    @Test
    fun testDependentModuleDeleteDependentHasNoErrorsBeforeAndAfterChange() {
        moduleInfoDao.stub {
            on { getModuleByName("dependent-before-change-module") } doReturn ModuleInfo(
                name = "dependent-before-change-module",
                connectionsTo = listOf(Connection("some-module")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module") } doReturn ModuleInfo(
                name = "some-module",
                connectionsTo = emptyList(),
                connectionsFrom = listOf(Connection("dependent-before-change-module")),
                codeRepository = null
            )
        }

        val errorsBeforeAndAfterChange = stableDependencyViolationFixHelper.testChangeDependencies(
            DependencyChange(
                dependencyChangeFromModuleName = "dependent-before-change-module",
                dependencyChangeToModuleName = "some-module",
                changingType = ChangingType.DELETE
            )
        )

        assertEquals(0, errorsBeforeAndAfterChange.first.size)
        assertEquals(0, errorsBeforeAndAfterChange.second.size)
    }

    @Test
    fun moreStableComponentStartToDependOnLessStableIncreaseErrors() {
        moduleInfoDao.stub {
            on { getModuleByName("more-stable-component") } doReturn ModuleInfo(
                name = "more-stable-component",
                connectionsTo = emptyList(),
                connectionsFrom = listOf(
                    Connection("some-module-1"),
                    Connection("some-module-2"),
                    Connection("some-module-3")
                ),
                codeRepository = null
            )
            on { getModuleByName("some-module-1") } doReturn ModuleInfo(
                name = "some-module-1",
                connectionsTo = listOf(Connection("more-stable-component")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module-2") } doReturn ModuleInfo(
                name = "some-module-2",
                connectionsTo = listOf(Connection("more-stable-component")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module-3") } doReturn ModuleInfo(
                name = "some-module-3",
                connectionsTo = listOf(Connection("more-stable-component")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("less-stable-component") } doReturn ModuleInfo(
                name = "less-stable-component",
                connectionsTo = listOf(Connection("some-component-4")),
                connectionsFrom = listOf(Connection("some-component-5")),
                codeRepository = null
            )
            on { getModuleByName("some-component-4") } doReturn ModuleInfo(
                name = "some-component-4",
                connectionsTo = emptyList(),
                connectionsFrom = listOf(Connection("less-stable-component")),
                codeRepository = null
            )
            on { getModuleByName("some-component-5") } doReturn ModuleInfo(
                name = "some-component-5",
                connectionsTo = listOf(Connection("less-stable-component")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
        }

        val errorsBeforeAndAfterChange = stableDependencyViolationFixHelper.testChangeDependencies(
            DependencyChange(
                dependencyChangeFromModuleName = "more-stable-component",
                dependencyChangeToModuleName = "less-stable-component",
                changingType = ChangingType.ADD
            )
        )

        assertEquals(0, errorsBeforeAndAfterChange.first.size)
        assertEquals(1, errorsBeforeAndAfterChange.second.size)
    }

    @Test
    fun moreStableComponentStopDependOnLessStableDecreaseErrors() {
        moduleInfoDao.stub {
            on { getModuleByName("more-stable-component") } doReturn ModuleInfo(
                name = "more-stable-component",
                connectionsTo = listOf(
                    Connection("less-stable-component")
                ),
                connectionsFrom = listOf(
                    Connection("some-module-1"),
                    Connection("some-module-2"),
                    Connection("some-module-3")
                ),
                codeRepository = null
            )
            on { getModuleByName("some-module-1") } doReturn ModuleInfo(
                name = "some-module-1",
                connectionsTo = listOf(Connection("more-stable-component")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module-2") } doReturn ModuleInfo(
                name = "some-module-2",
                connectionsTo = listOf(Connection("more-stable-component")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module-3") } doReturn ModuleInfo(
                name = "some-module-3",
                connectionsTo = listOf(Connection("more-stable-component")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("less-stable-component") } doReturn ModuleInfo(
                name = "less-stable-component",
                connectionsTo = listOf(Connection("some-component-4")),
                connectionsFrom = listOf(Connection("some-component-5"), Connection("more-stable-component")),
                codeRepository = null
            )
            on { getModuleByName("some-component-4") } doReturn ModuleInfo(
                name = "some-component-4",
                connectionsTo = emptyList(),
                connectionsFrom = listOf(Connection("less-stable-component")),
                codeRepository = null
            )
            on { getModuleByName("some-component-5") } doReturn ModuleInfo(
                name = "some-component-5",
                connectionsTo = listOf(Connection("less-stable-component")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
        }

        val errorsBeforeAndAfterChange = stableDependencyViolationFixHelper.testChangeDependencies(
            DependencyChange(
                dependencyChangeFromModuleName = "more-stable-component",
                dependencyChangeToModuleName = "less-stable-component",
                changingType = ChangingType.DELETE
            )
        )

        assertEquals(1, errorsBeforeAndAfterChange.first.size)
        assertEquals(0, errorsBeforeAndAfterChange.second.size)
    }

    @Test
    fun getErrorOnRelatedToDependencyChangesModules() {
        moduleInfoDao.stub {
            on { getModuleByName("dependency-from-change-module") } doReturn ModuleInfo(
                name = "dependency-from-change-module",
                connectionsTo = emptyList(),
                connectionsFrom = listOf(
                    Connection("some-module-1"),
                    Connection("some-module-2"),
                    Connection("module-with-error-after-change-but-not-changing-itself")
                ),
                codeRepository = null
            )
            on { getModuleByName("some-module-1") } doReturn ModuleInfo(
                name = "some-module-1",
                connectionsTo = listOf(Connection("dependency-from-change-module")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module-2") } doReturn ModuleInfo(
                name = "some-module-2",
                connectionsTo = listOf(Connection("dependency-from-change-module")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("module-with-error-after-change-but-not-changing-itself") } doReturn ModuleInfo(
                name = "module-with-error-after-change-but-not-changing-itself",
                connectionsTo = listOf(Connection("dependency-from-change-module")),
                connectionsFrom = listOf(
                    Connection("some-module-6"),
                    Connection("some-module-7"),
                    Connection("some-module-8"),
                    Connection("some-module-9")
                ),
                codeRepository = null
            )
            on { getModuleByName("some-module-6") } doReturn ModuleInfo(
                name = "some-module-6",
                connectionsTo = listOf(Connection("module-with-error-after-change-but-not-changing-itself")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module-7") } doReturn ModuleInfo(
                name = "some-module-7",
                connectionsTo = listOf(Connection("module-with-error-after-change-but-not-changing-itself")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module-8") } doReturn ModuleInfo(
                name = "some-module-8",
                connectionsTo = listOf(Connection("module-with-error-after-change-but-not-changing-itself")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-module-9") } doReturn ModuleInfo(
                name = "some-module-9",
                connectionsTo = listOf(Connection("module-with-error-after-change-but-not-changing-itself")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("dependency-to-change-module") } doReturn ModuleInfo(
                name = "dependency-to-change-module",
                connectionsTo = listOf(Connection("some-component-3")),
                connectionsFrom = listOf(Connection("some-component-4"), Connection("some-component-5")),
                codeRepository = null
            )
            on { getModuleByName("some-component-3") } doReturn ModuleInfo(
                name = "some-component-3",
                connectionsTo = emptyList(),
                connectionsFrom = listOf(Connection("dependency-to-change-module")),
                codeRepository = null
            )
            on { getModuleByName("some-component-4") } doReturn ModuleInfo(
                name = "some-component-4",
                connectionsTo = listOf(Connection("dependency-to-change-module")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
            on { getModuleByName("some-component-5") } doReturn ModuleInfo(
                name = "some-component-4",
                connectionsTo = listOf(Connection("dependency-to-change-module")),
                connectionsFrom = emptyList(),
                codeRepository = null
            )
        }

        val errorsBeforeAndAfterChange = stableDependencyViolationFixHelper.testChangeDependencies(
            DependencyChange(
                dependencyChangeFromModuleName = "dependency-from-change-module",
                dependencyChangeToModuleName = "dependency-to-change-module",
                changingType = ChangingType.ADD
            )
        )

        assertEquals(0, errorsBeforeAndAfterChange.first.size)
        assertEquals(1, errorsBeforeAndAfterChange.second.size)
    }
}
