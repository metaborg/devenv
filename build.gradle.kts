plugins {
  id("org.metaborg.gradle.config.devenv") version "0.3.21"
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
}

devenv {
  repoUrlPrefix = "git@github.com:metaborg"

  // Gradle plugins.
  registerRepo("gradle.config")
  registerRepo("gitonium")
  registerRepo("coronium")
  registerRepo("spoofax.gradle")

  // Spoofax Core libraries and applications.
  registerRepo("mb-exec")
  registerRepo("nabl")
  registerRepo("spoofax")
  registerRepo("sdf")
  registerRepo("jsglr")

  // Libraries and applications.
  registerRepo("log")
  registerRepo("resource")
  registerRepo("pie")
  registerRepo("spoofax-pie")

  // Continuous integration.
  registerRepo("jenkins.pipeline")
}

tasksWithIncludedBuild("gitonium") {
  registerDelegateTask("buildGitonium", it, ":buildAll")
}

tasksWithIncludedBuild("coronium") { coronium ->
  tasksWithIncludedBuild("coronium.example") { coroniumExample ->
    register("cleanCoronium") {
      group = "development"
      dependsOn(coronium.task(":cleanAll"))
      dependsOn(coroniumExample.task(":cleanAll"))
    }
    register("buildCoronium") {
      group = "development"
      dependsOn(coronium.task(":buildAll"))
      dependsOn(coroniumExample.task(":buildAll"))
    }
  }
}

tasksWithIncludedBuild("spoofax.gradle") { spoofaxGradle ->
  tasksWithIncludedBuild("spoofax.gradle.example") { spoofaxGradleExample ->
    register("cleanSpoofaxGradle") {
      group = "development"
      dependsOn(spoofaxGradle.task(":cleanAll"))
      dependsOn(spoofaxGradleExample.task(":cleanAll"))
    }
    register("buildSpoofaxGradle") {
      group = "development"
      dependsOn(spoofaxGradle.task(":buildAll"))
      dependsOn(spoofaxGradleExample.task(":buildAll"))
    }
  }
}

tasksWithIncludedBuild("pie") {
  registerDelegateTask("buildPie", it, ":buildAll")
  registerDelegateTask("publishPieLangToMavenLocal", it, ":pie.lang:publishToMavenLocal")
}

tasksWithIncludedBuild("sdf") {
  registerDelegateTask("buildSdf3", it, ":buildAll")
  registerDelegateTask("buildSdf3Lang", it, ":org.metaborg.meta.lang.template:build")
}

tasksWithIncludedBuild("spoofax.example") {
  registerDelegateTask("buildSpoofaxExample", it, ":buildAll")

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
  registerDelegateTask("testSdf3Spoofax", it, ":sdf3.spoofax:test")
  registerDelegateTask("runSdf3Cli", it, ":sdf3.cli:run")
  registerDelegateTask("runSdf3Eclipse", it, ":sdf3.eclipse:runEclipse")
  registerDelegateTask("runSdf3IntelliJ", it, ":sdf3.intellij:runIde")
}

fun Project.tasksWithIncludedBuild(name: String, fn: TaskContainer.(IncludedBuild) -> Unit) {
  try {
    tasks.fn(gradle.includedBuild(name))
  } catch(e: UnknownDomainObjectException) {
    // Ignore
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
  register("runTaskInCompositeBuild") {
    this.group = "composite build"
    this.description = "Runs a task in a composite build. Task path is given via -Ptask and composite build name is given via -PcompositeBuild"
    dependsOn(run {
      val taskPath = gradle.rootProject.property("task")
        ?: throw GradleException("Task name was not given. Give the name of the task to execute with -Ptask=<task path>")
      val compositeBuildName = gradle.rootProject.property("compositeBuild")
        ?: throw GradleException("Composite build name was not given. Give the name of the composite build to execute the task in with -PcompositeBuild=<composite build name>")
      val compositeBuild = gradle.includedBuild(compositeBuildName.toString())
      compositeBuild.task(taskPath.toString())
    })
  }
}
