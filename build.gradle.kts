plugins {
  id("org.metaborg.gradle.config.devenv") version "0.3.20"
  id("org.metaborg.gradle.config.root-project") version "0.3.20"
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

try {
  val pie = gradle.includedBuild("pie")
  tasks.register("buildPie") {
    dependsOn(pie.task(":buildAll"))
  }
} catch(e: UnknownDomainObjectException) {
  // Ignore
}
tasks.register("buildTigerManual") {
  dependsOn(gradle.includedBuild("spoofax.example.tiger.manual").task(":buildAll"))
}
tasks.register("runSdf3Eclipse") {
  dependsOn(gradle.includedBuild("spoofax.example.sdf3").task(":sdf3.eclipse:run"))
}
tasks.register("testSdf3Spoofax") {
  dependsOn(gradle.includedBuild("spoofax.example.sdf3").task(":sdf3.spoofax:test"))
}

