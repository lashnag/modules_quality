package ru.lashnev.modules.quality.service

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.tomcat.util.http.fileupload.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig
import org.eclipse.jgit.util.FS
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import ru.lashnev.modules.quality.config.GitProperties
import ru.lashnev.modules.quality.dao.ModuleInfoDao
import ru.lashnev.modules.quality.exceptions.ModuleWithoutRepositoryException
import ru.lashnev.modules.quality.model.CodeRepository
import ru.lashnev.modules.quality.model.LocalRepository
import ru.lashnev.modules.quality.model.RepositoryType
import java.nio.file.Files
import java.util.Base64
import kotlin.io.path.pathString

@Service
class GitDownloaderService(
    private val moduleInfoDao: ModuleInfoDao,
    private val gitProperties: GitProperties
) : ProjectDownloaderService {

    private lateinit var uriToGitKey: String

    init {
        val pathToGitKey = Files.createTempFile(null, null)
        val decodedPrivateKeyBytes: ByteArray = Base64.getDecoder().decode(gitProperties.base64PrivateKey)
        Files.write(pathToGitKey, decodedPrivateKeyBytes)
        uriToGitKey = pathToGitKey.pathString
    }

    @Cacheable(cacheNames = ["local-repositories"], cacheManager = "localRepositoriesCacheManager")
    override fun downloadProject(moduleName: String): LocalRepository {
        val tempDirectory = Files.createTempDirectory(moduleName)
        val cloneCommand = Git.cloneRepository()
        moduleInfoDao.getModuleByName(moduleName).codeRepository?.let {
            cloneCommand.setURI(makeSshUri(it))
        } ?: throw ModuleWithoutRepositoryException()
        cloneCommand.setTransportConfigCallback { transport ->
            val sshTransport = transport as SshTransport
            sshTransport.sshSessionFactory = sshSessionFactory
        }
        cloneCommand.setDirectory(tempDirectory.toFile())
        try {
            cloneCommand.call()
        } catch (e: Throwable) {
            FileUtils.deleteDirectory(tempDirectory.toFile())
            throw e
        }

        return LocalRepository(tempDirectory)
    }

    val sshSessionFactory: SshSessionFactory = object : JschConfigSessionFactory() {
        override fun configure(host: OpenSshConfig.Host, session: Session) {
            session.setConfig("StrictHostKeyChecking", "no")
        }

        override fun createDefaultJSch(fs: FS): JSch {
            val defaultJSch = super.createDefaultJSch(fs)
            defaultJSch.addIdentity(uriToGitKey)
            return defaultJSch
        }
    }

    private fun makeSshUri(codeRepository: CodeRepository): String {
        return when (codeRepository.type) {
            RepositoryType.GERRIT ->
                codeRepository
                    .link
                    .replace("https", "ssh")
                    .replace(".ru", ".ru:${gitProperties.gerritPort}")
                    .replace("//", "//${gitProperties.gerritLogin}@")
                    .replace("admin/repos/", "")
            RepositoryType.GITHUB ->
                codeRepository
                    .link
                    .replace("https", "ssh")
                    .replace(".ru", ".ru:${gitProperties.githubPort}")
                    .replace("//", "//git@")
        }
    }
}
