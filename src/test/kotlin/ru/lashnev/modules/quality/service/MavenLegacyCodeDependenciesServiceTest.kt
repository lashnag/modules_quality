package ru.lashnev.modules.quality.service

import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import ru.lashnev.modules.quality.config.LegacyCodeDependenciesProperties
import ru.lashnev.modules.quality.model.LocalRepository
import ru.lashnev.modules.quality.model.WarningType
import kotlin.io.path.Path
import kotlin.test.assertTrue

@ActiveProfiles("test")
@SpringBootTest(classes = [MavenLegacyCodeDependenciesService::class])
@EnableConfigurationProperties(LegacyCodeDependenciesProperties::class)
internal class MavenLegacyCodeDependenciesServiceTest {

    @Autowired
    private lateinit var mavenLegacyCodeDependenciesService: MavenLegacyCodeDependenciesService

    @Autowired
    private lateinit var legacyCodeDependenciesProperties: LegacyCodeDependenciesProperties

    @MockBean
    private lateinit var projectDownloaderService: ProjectDownloaderService

    private val pathToDependenciesNewProject = "src/test/sample/projects/dependencies_new"
    private val pathToDependenciesOldProject = "src/test/sample/projects/dependencies_old"
    private val pathToEmptyDependenciesProject = "src/test/sample/projects/dependencies_empty"
    private val pathToNoVersionDependenciesProject = "src/test/sample/projects/dependencies_no_version"
    private val pathToParentNotFoundProject = "src/test/sample/projects/dependencies_not_found"

    @Test
    fun getOldPlatformWarning() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToDependenciesOldProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.any { it.type == WarningType.LEGACY_PARENT_DEPENDENCY })
    }

    @Test
    fun noWarningIfNoParentDependency() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToEmptyDependenciesProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.none { it.type == WarningType.LEGACY_PARENT_DEPENDENCY })
    }

    @Test
    fun noWarningIfParentIsNotPlatform() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToParentNotFoundProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.none { it.type == WarningType.LEGACY_PARENT_DEPENDENCY })
    }

    @Test
    fun noWarningIfParentHasNoVersion() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToNoVersionDependenciesProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.none { it.type == WarningType.LEGACY_PARENT_DEPENDENCY })
    }

    @Test
    fun noWarningIfPlatformIsNew() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToDependenciesNewProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.none { it.type == WarningType.LEGACY_PARENT_DEPENDENCY })
    }

    @Test
    fun getOldDependencyWarning() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToDependenciesOldProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.any { it.type == WarningType.LEGACY_CODE_DEPENDENCY })
    }

    @Test
    fun noWarningIfNoCheckingDependency() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToEmptyDependenciesProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.none { it.type == WarningType.LEGACY_CODE_DEPENDENCY })
    }

    @Test
    fun noWarningIfDependencyIsNew() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToDependenciesNewProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.none { it.type == WarningType.LEGACY_CODE_DEPENDENCY })
    }

    @Test
    fun noWarningIfDependencyHasNoVersion() {
        projectDownloaderService.stub {
            on { downloadProject("some-module") } doReturn LocalRepository(Path(pathToNoVersionDependenciesProject))
        }
        val warnings = mavenLegacyCodeDependenciesService.getWarnings("some-module")
        assertTrue(warnings.none { it.type == WarningType.LEGACY_CODE_DEPENDENCY })
    }
}
