package ru.lashnev.modules.quality.service.analyzers

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.config.CompilerConfiguration

fun createPsiFactoryInstance(): PsiFileFactory {
    val configuration = CompilerConfiguration()

    configuration.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false)
    )

    return PsiFileFactory.getInstance(
        KotlinCoreEnvironment.createForProduction(
            parentDisposable = Disposer.newDisposable(),
            configuration = configuration,
            configFiles = EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).project
    )
}
