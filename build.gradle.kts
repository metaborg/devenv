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
}

tasksWithIncludedBuild("spoofax.example") {
  registerDelegateTask("buildSpoofaxExample", it, ":buildAll")

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
