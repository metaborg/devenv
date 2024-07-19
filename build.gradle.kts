// Apply plugin the old way for compatibility with both Gradle 5.6.4 and 6+.
buildscript {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral()
    }
    dependencies {
        classpath("org.metaborg:gradle.config:0.7.1")
    }
}

tasks.register("includedBuilds") {
    doLast {
        println("Included builds")
        println("---------------")
        for (build in gradle.includedBuilds) {
            println(":${build.name}")
        }
    }
}

// Gitonium
tasks.register("buildGitonium") {
    dependsOn(gradle.includedBuild("gitonium").task(":buildAll"))
}

tasksWithIncludedBuild("pie.core.root") { pieCore ->
    tasksWithIncludedBuild("pie.lang.root") { pieLang ->
        register("buildPie") {
            group = "development"
            dependsOn(pieCore.task(":build"))
            dependsOn(pieLang.task(":build"))
        }
    }
}

tasksWithIncludedBuild("pie.lang.root") {
    registerDelegateTask("publishPieLangToMavenLocal", it, ":pie.lang:publishToMavenLocal")
}

tasksWithIncludedBuild("sdf") {
    registerDelegateTask("buildSdf3", it, ":buildAll")
    registerDelegateTask("buildSdf3Lang", it, ":org.metaborg.meta.lang.template:build")
}

tasksWithIncludedBuild("stratego") {
    registerDelegateTask("buildStratego", it, ":buildAll")
    registerDelegateTask("buildStrategoLang", it, ":org.metaborg.meta.lang.stratego:build")
}

tasksWithIncludedBuild("spoofax3.example.root") {
    registerDelegateTask("buildSpoofax3Examples", it, ":buildAll")

    registerDelegateTask("testTiger", it, ":tiger:test")
    registerDelegateTask("testTigerSpoofax", it, ":tiger.spoofax:test")
    registerDelegateTask("runTigerCli", it, ":tiger.cli:run")
    registerDelegateTask("runTigerEclipse", it, ":tiger.eclipse:runEclipse")
    registerDelegateTask("runTigerIntelliJ", it, ":tiger.intellij:runIde")

    registerDelegateTask("testMod", it, ":mod:test")
    registerDelegateTask("testModSpoofax", it, ":mod.spoofax:test")
    registerDelegateTask("runModCli", it, ":mod.cli:run")
    registerDelegateTask("runModEclipse", it, ":mod.eclipse:runEclipse")
    registerDelegateTask("runModIntelliJ", it, ":mod.intellij:runIde")

    registerDelegateTask("testSdf3", it, ":sdf3:test")
    registerDelegateTask("runSdf3Cli", it, ":sdf3.cli:run")
    registerDelegateTask("runSdf3Eclipse", it, ":sdf3.eclipse:runEclipse")
    registerDelegateTask("runSdf3IntelliJ", it, ":sdf3.intellij:runIde")
}

gradle.includedBuild("spoofax3.root").let { spoofaxPie ->
    tasks.register("buildSpoofax3Lwb") {
        dependsOn(spoofaxPie.task(":buildSpoofax3Lwb"))
    }
    tasks.register("runSpoofax3LwbEclipse") {
        dependsOn(spoofaxPie.task(":runSpoofax3LwbEclipse"))
    }
    tasks.register("buildSpoofax3LwbEclipseInstallation") {
        dependsOn(spoofaxPie.task(":buildSpoofax3LwbEclipseInstallation"))
    }
    tasks.register("buildSpoofax3LwbEclipseInstallationWithJvm") {
        dependsOn(spoofaxPie.task(":buildSpoofax3LwbEclipseInstallationWithJvm"))
    }
    tasks.register("publishSpoofax3Lwb") {
        dependsOn(spoofaxPie.task(":publishSpoofax3Lwb"))
    }
    tasks.register("archiveSpoofax3LwbEclipseInstallations") {
        dependsOn(spoofaxPie.task(":archiveSpoofax3LwbEclipseInstallations"))
    }
}


fun Project.tasksWithIncludedBuild(name: String, fn: TaskContainer.(IncludedBuild) -> Unit) {
    try {
        tasks.fn(gradle.includedBuild(name))
    } catch (e: UnknownDomainObjectException) {
        // Ignore
        logger.warn("Included build not found, probably disabled: $name")
    }
}

fun TaskContainer.registerDelegateTask(name: String, build: IncludedBuild, vararg taskPaths: String) {
    this.register(name) {
        group = "development"
        taskPaths.forEach {
            dependsOn(build.task(it))
        }
    }
}

tasks {
    register("runTasksInCompositeBuild") {
        this.group = "composite build"
        this.description = "Runs tasks in a composite build. Task paths are given via -PtaskPaths (separated by ;) and the name of the composite build is given via -PcompositeBuildName"

        try {
            val taskPaths = gradle.rootProject.property("taskPaths").toString().split(";")
            val compositeBuildName = gradle.rootProject.property("compositeBuildName")
            val compositeBuild = gradle.includedBuild(compositeBuildName.toString())
            dependsOn(taskPaths.map { compositeBuild.task(it) })
        } catch (e: groovy.lang.MissingPropertyException) {
            // Ignore to prevent errors during configuration
        } catch (e: UnknownDomainObjectException) {
            // Ignore to prevent errors during configuration
        }
    }
}

// Auto-accept build scan TOS
extensions.findByName("buildScan")?.withGroovyBuilder {
    try {
        // New Developcity plugin
        setProperty("termsOfUseUrl", "https://gradle.com/help/legal-terms-of-use")
        setProperty("termsOfUseAgree", "yes")
    } catch (ex: groovy.lang.MissingPropertyException) {
        // Deprecated Gradle Enterprise plugin
        setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
        setProperty("termsOfServiceAgree", "yes")
    }
}

// Normally, when you execute a task such as `test` in a multi-project build, you will execute
//  all `:test` tasks in all projects. In contrast, when you specifically execute `:test`
//  (prefixed with a colon), you execute the `:test` task only in the root project.
// Now, we would like to create a task in this composite build `testAll` that executes basically
//  the equivalent of `test` in each of the included multi-project builds, which would execute
//  `:test` in each of the projects. However, this seems to be impossible to write down. Instead,
//  we call the root `:test` task in the included build, and in each included build's multi-project
//  root project we'll extend the `test` task to depend on the `:test` tasks of the subprojects.


// Build tasks
tasks.register("assembleAll") {
    group = "Build"
    description = "Assembles the outputs of the subprojects and included builds."
    dependsOnAll("assemble")
}
tasks.register("buildAll") {
    group = "Build"
    description = "Assembles and tests the subprojects and included builds."
    dependsOnAll("build")
}
tasks.register("cleanAll") {
    group = "Build"
    description = "Cleans the outputs of the subprojects and included builds."
    dependsOnAll("clean")
}

// Publishing tasks
tasks.register("publishAll") {
    group = "Publishing"
    description = "Publishes all subprojects and included builds to a remote Maven repository."
    dependsOn(gradle.includedBuilds.filter { it.name !in listOf("gitonium", "coronium") }.map { it.task(":publish") })
    dependsOn(gradle.includedBuild("gitonium").task(":publishAllPublicationsToMetaborgArtifactsRepository"))
    dependsOn(gradle.includedBuild("coronium").task(":publishAllPublicationsToMetaborgArtifactsRepository"))
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("publish") })
}
tasks.register("publishAllToMavenLocal") {
    group = "Publishing"
    description = "Publishes all subprojects and included builds to the local Maven repository."
    dependsOnAll("publishToMavenLocal")
}

// Verification tasks
tasks.register("checkAll") {
    group = "Verification"
    description = "Runs all checks on the subprojects and included builds."
    dependsOnAll("check")
}
tasks.register("testAll") {
    group = "Verification"
    description = "Runs all unit tests on the subprojects and included builds."
    dependsOnAll("test")
}

// Help tasks
tasks.register("allTasks") {
    group = "Help"
    description = "Displays all tasks of subprojects and included builds."
    dependsOnAll("tasks")
}

fun Task.dependsOnAll(taskName: String) {
    dependsOn(gradle.includedBuilds.map { it.task(":$taskName") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName(taskName) })
}
