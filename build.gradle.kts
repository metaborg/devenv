// Apply plugin the old way for compatibility with both Gradle 5.6.4 and 6+.
buildscript {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
    dependencies {
        classpath("org.metaborg:gradle.config:0.5.6")
    }
}
apply(plugin = "org.metaborg.gradle.config.root-project")

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

// Coronium
gradle.includedBuild("coronium.root").let { coronium ->
    tasks.register("buildCoronium") {
        dependsOn(coronium.task(":buildAll"))
    }
    tasks.register("cleanCoronium") {
        dependsOn(coronium.task(":cleanAll"))
    }
}

// Spoofax Gradle
//gradle.includedBuild("spoofax.gradle.root").let { spoofaxGradle ->
//    tasks.register("buildSpoofaxGradle") {
//        group = "Development"
//        dependsOn(spoofaxGradle.task(":buildAll"))
//    }
//    tasks.register("cleanSpoofaxGradle") {
//        group = "Development"
//        dependsOn(spoofaxGradle.task(":cleanAll"))
//    }
//}


tasksWithIncludedBuild("pie.core.root") { pieCore ->
    tasksWithIncludedBuild("pie.lang.root") { pieLang ->
        register("buildPie") {
            group = "development"
            dependsOn(pieCore.task(":buildAll"))
            dependsOn(pieLang.task(":buildAll"))
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
        logger.warn("Included build $name not found")
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
