package com.mikepenz.aboutlibraries.plugin

import com.mikepenz.aboutlibraries.plugin.model.CollectedContainer
import com.mikepenz.aboutlibraries.plugin.util.DependencyCollector
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory
import java.io.File

@CacheableTask
abstract class AboutLibrariesCollectorTask : DefaultTask() {

    @Internal
    protected val extension = project.extensions.getByName("aboutLibraries") as AboutLibrariesExtension

    @Input
    val projectName = project.name

    @Input
    val includePlatform = extension.includePlatform

    @Input
    val filterVariants = extension.filterVariants

    /** holds the collected set of dependencies*/
    @Internal
    protected lateinit var collectedDependencies: CollectedContainer

    @OutputFile
    val dependencyCache: File = File(File(project.buildDir, "generated/aboutLibraries/").also {
        it.mkdirs()
    }, "dependency_cache.json")

    /**
     * Collect the dependencies via the available configurations for the current project
     */
    fun configure() {
        project.evaluationDependsOnChildren()
        collectedDependencies = DependencyCollector(includePlatform, filterVariants).collect(project)
    }

    @TaskAction
    fun action() {
        LOGGER.info("Collecting for: $projectName")
        if (!::collectedDependencies.isInitialized) {
            configure()
        }
        dependencyCache.writeText(groovy.json.JsonOutput.toJson(collectedDependencies))
    }

    private companion object {
        private val LOGGER = LoggerFactory.getLogger(AboutLibrariesCollectorTask::class.java)!!
    }
}