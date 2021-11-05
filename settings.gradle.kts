rootProject.name = "devenv"

// Make plugin repositories available, for loading plugins in included builds.
pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

// Apply devenv-settings plugin. Settings plugins must still be put on the classpath via a buildscript block.
buildscript {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
  dependencies {
    classpath("org.metaborg:gradle.config:0.4.7")
  }
}
apply(plugin = "org.metaborg.gradle.config.devenv-settings")

// Include builds from subdirectories, but only if it is from an included repository.
// The order of these includes is important. Gradle plugins must be included BEFORE they are used!
// Manually include nested composite builds, as IntelliJ does not support them.
configure<mb.gradle.config.devenv.DevenvSettingsExtension> {
  // Independent Gradle plugins.
  includeBuildIfRepositoryIncluded("gradle.config")
  includeBuildIfRepositoryIncluded("gitonium")
  if(isRepositoryIncluded("coronium")) {
    includeBuildWithName("coronium", "coronium.root")
    includeBuildWithName("coronium/plugin", "coronium")
    includeBuildWithName("coronium/example", "coronium.example")
  }

  // Independent common Java libraries.
  if(isRepositoryIncluded("log")) {
    includeBuildWithName("log", "log.root")
  }
  if(isRepositoryIncluded("resource")) {
    includeBuildWithName("resource", "resource.root")
  }
  if(isRepositoryIncluded("common")) {
    includeBuildWithName("common", "common.root")
  }

  // PIE Java libraries.
  if(isRepositoryIncluded("pie")) {
    includeBuildWithName("pie", "pie.root")
    includeBuildWithName("pie/core", "pie.core.root")
    includeBuildWithName("pie/bench", "pie.bench")
  }

  // Spoofax 2 Java libraries, languages, and Gradle plugin.
  if(isRepositoryIncluded("releng")) {
    includeBuildWithName("releng/gradle", "spoofax2.releng.root")
    includeBuildWithName("releng/gradle/java", "spoofax2.releng.java.root")
    includeBuildWithName("releng/gradle/language", "spoofax2.releng.language.root")
  }

  // PIE DSL (include after Spoofax 2, since it uses the Spoofax 2 Gradle plugin)
  if(isRepositoryIncluded("pie")) {
    includeBuildWithName("pie/lang", "pie.lang.root")
  }

  // Spoofax 3 Java libraries, languages, and Gradle plugins.
  if(isRepositoryIncluded("spoofax-pie")) {
    includeBuildWithName("spoofax.pie", "spoofax3.root")
    includeBuildWithName("spoofax.pie/core", "spoofax3.core.root")
    includeBuildWithName("spoofax.pie/metalib", "spoofax3.metalib.root")
    includeBuildWithName("spoofax.pie/lwb", "spoofax3.lwb.root")
    includeBuildWithName("spoofax.pie/lwb.distrib", "spoofax3.lwb.distrib.root")
    includeBuildWithName("spoofax.pie/example", "spoofax3.example.root")
  }

  // Jenkins CI
  includeBuildIfRepositoryIncluded("jenkins.pipeline")
}
