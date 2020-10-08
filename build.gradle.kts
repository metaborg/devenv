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
  registerRepo("jsglr")
  registerRepo("sdf")
  registerRepo("stratego")
  registerRepo("esv")
  registerRepo("nabl")
  registerRepo("spoofax2")
  registerRepo("releng")

  // Libraries and applications.
  registerRepo("log")
  registerRepo("resource")
  registerRepo("pie")
  registerRepo("spoofax-pie")

  // Continuous integration.
  registerRepo("jenkins.pipeline")
}

tasks.register("includedBuilds") {
  doLast {
    println("Included builds:")
    for (build in gradle.includedBuilds) {
      println("  :${build.name} @ ${build.projectDir}")
    }
  }
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
  register("runTasksInCompositeBuild") {
    this.group = "composite build"
    this.description = "Runs tasks in a composite build. Task paths are given via -PtaskPaths (separated by ;) and the name of the composite build is given via -PcompositeBuildName"

    try {
      val taskPaths = gradle.rootProject.property("taskPaths").toString().split(";")
      val compositeBuildName = gradle.rootProject.property("compositeBuildName")
      val compositeBuild = gradle.includedBuild(compositeBuildName.toString())
      dependsOn(taskPaths.map { compositeBuild.task(it) })
    } catch(e: groovy.lang.MissingPropertyException) {
      // Ignore to prevent errors during configuration
    } catch(e: UnknownDomainObjectException) {
      // Ignore to prevent errors during configuration
    }
  }
}

// Auto-accept build scan TOS
extensions.findByName("buildScan")?.withGroovyBuilder {
  setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
  setProperty("termsOfServiceAgree", "yes")
}
