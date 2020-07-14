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
  registerRepo("mb-exec", defaultBranch = "master", defaultDirPath = "mb.exec")
  registerRepo("jsglr", defaultBranch = "master")
  registerRepo("sdf", defaultBranch = "spoofax3")
  registerRepo("stratego", defaultBranch = "spoofax3")
  registerRepo("nabl", defaultBranch = "master")
  registerRepo("spoofax2", defaultUrl = "git@github.com:metaborg/spoofax", defaultBranch = "spoofax3")
  registerRepo("releng", defaultUrl = "git@github.com:metaborg/spoofax-deploy", defaultBranch = "spoofax3")

  // Libraries and applications.
  registerRepo("log")
  registerRepo("resource")
  registerRepo("pie")
  registerRepo("spoofax-pie", defaultDirPath = "spoofax.pie")

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

    try {
      val taskPath = gradle.rootProject.property("task")
      val compositeBuildName = gradle.rootProject.property("compositeBuild")
      val compositeBuild = gradle.includedBuild(compositeBuildName.toString())
      dependsOn(compositeBuild.task(taskPath.toString()))
    } catch(e: groovy.lang.MissingPropertyException) {
      // Ignore to prevent errors during configuration
    } catch(e: UnknownDomainObjectException) {
      // Ignore to prevent errors during configuration
    }
  }
}
