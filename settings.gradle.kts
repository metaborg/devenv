rootProject.name = "devenv"

// Make plugin repositories available, for loading plugins in included builds.
pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

plugins {
  id("com.gradle.enterprise") version("3.17.3")
}


// Apply devenv-settings plugin. Settings plugins must still be put on the classpath via a buildscript block.
buildscript {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
  dependencies {
    classpath("org.metaborg:gradle.config:0.5.6")
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
  }

  // Spoofax 2 Java libraries, languages, and Gradle plugin.
  if(isRepositoryIncluded("releng")) {
    includeBuildWithName("releng/gradle", "spoofax2.releng.root")
  }

  // PIE DSL (include after Spoofax 2, since it uses the Spoofax 2 Gradle plugin)
  if(isRepositoryIncluded("pie")) {
    includeBuildWithName("pie/lang", "pie.lang.root")
  }

  // Spoofax 3 Java libraries, languages, and Gradle plugins.
  if(isRepositoryIncluded("spoofax-pie")) {
    includeBuildWithName("spoofax.pie", "spoofax3.root")
  }

  // Jenkins CI
  includeBuildIfRepositoryIncluded("jenkins.pipeline")
}

gradleEnterprise {
  buildScan {
    if (!System.getenv("CI").isNullOrEmpty()) {
      termsOfServiceUrl = "https://gradle.com/terms-of-service"
      termsOfServiceAgree = "yes"
      publishAlways()
      tag("CI")
    }
  }
}
