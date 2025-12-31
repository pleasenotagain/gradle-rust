package fr.stardustenterprises.gradle.rust.wrapper.task

import fr.stardustenterprises.gradle.rust.wrapper.TargetManager
import fr.stardustenterprises.gradle.rust.wrapper.ext.WrapperExtension
import fr.stardustenterprises.stargrad.task.ConfigurableTask
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecOperations
import org.gradle.process.internal.ExecException
import javax.inject.Inject

abstract class WrappedTask(
    @Internal
    protected val command: String
) : ConfigurableTask<WrapperExtension>() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @Throws(ExecException::class)
    override fun run() {
        TargetManager.ensureTargetsInstalled(execOperations, configuration)

        configuration.targets.forEach { target ->
            execOperations.exec {
                it.commandLine(target.command)
                it.args(target.subcommand(command))
                it.workingDir(
                    configuration.crate.asFile.orNull
                        ?: throw RuntimeException("Invalid working dir.")
                )
                it.environment(target.env)
            }.assertNormalExitValue()
        }
    }
}
