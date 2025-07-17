package fr.stardustenterprises.gradle.rust.wrapper

import fr.stardustenterprises.gradle.rust.wrapper.ext.WrapperExtension
import org.gradle.api.Named

data class TargetOptions(
    private val name: String,
    var command: String? = null,
    var toolchain: String? = null,
    var target: String? = null,
    var release: Boolean? = null,
    var outputName: String? = null,
    var args: MutableList<String> = mutableListOf("_DEFAULT"),
    var env: MutableMap<String, String> = mutableMapOf("_DEFAULT" to "DEFAULT"),
) : Named {
    constructor(name: String) :
        this(
            name,
            null,
            null,
            null,
            null,
            null
        )

    /**
     * The object's name.
     *
     * Must be constant for the life of the object.
     *
     * @return The name. Never null.
     */
    override fun getName(): String =
        name

    fun subcommand(vararg sub: String): List<String> {
        // cargo [+toolchain] [OPTIONS] [SUBCOMMAND]
        val cmd = mutableListOf<String>()

        if (!toolchain.isNullOrBlank()) {
            if (!toolchain!!.startsWith('+')) {
                toolchain = "+$toolchain"
            }
            cmd += toolchain!!
        }

        cmd.addAll(sub)

        cmd += "--target=$target"

        if (release == true) {
            cmd += "--release"
        }

        if (!this.args.contains("_DEFAULT")) {
            cmd.addAll(this.args)
        }

        return cmd
    }

    fun populateFrom(configuration: WrapperExtension) {
        if (this.target.isNullOrBlank()) {
            throw RuntimeException("Please input the target platform name.")
        }

        if (this.outputName.isNullOrBlank()) {
            throw RuntimeException("Please input a target output name.")
        }

        if (this.command.isNullOrBlank()) {
            this.command = configuration.command.orNull
                ?: throw RuntimeException("Invalid base command.")
        }
        if (this.toolchain.isNullOrBlank()) {
            this.toolchain = configuration.toolchain.orNull
                ?: throw RuntimeException("Invalid base toolchain.")
        }
        if (this.release == null) {
            this.release = configuration.release.getOrElse(false)
        }

        if (this.args.isEmpty()) {
            this.args = configuration.args
        } else {
            if (this.args.contains("_DEFAULT")) {
                val argsList = this.args.toList()
                val finalList = mutableListOf<String>()
                val index = argsList.indexOfFirst { it == "_DEFAULT" }
                val before = argsList.subList(0, index)
                val after = if (argsList.size == index)
                    emptyList()
                else
                    argsList.subList(index + 1, argsList.size)

                finalList.addAll(before)
                finalList.addAll(configuration.args.toList())
                finalList.addAll(after)

                this.args = finalList
            }
        }

        if (this.env.isEmpty()) {
            this.env = configuration.env
        } else {
            if (this.env.containsKey("_DEFAULT")) {
                val envVarList = this.env.toList()
                val finalList = mutableListOf<Pair<String, String>>()
                val index = envVarList.indexOfFirst { it.first == "_DEFAULT" }
                val before = envVarList.subList(0, index)
                val after = if (envVarList.size == index)
                    emptyList()
                else
                    envVarList.subList(index + 1, envVarList.size)

                finalList.addAll(before)
                finalList.addAll(configuration.env.toList())
                finalList.addAll(after)

                this.env = mutableMapOf()
                finalList.toMap(this.env)
            }
        }
    }
}
