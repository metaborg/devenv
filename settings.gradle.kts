import mb.gradle.config.devenv.DevenvSettingsExtension

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
    classpath("org.metaborg:gradle.config:0.3.21")
  }
}
apply(plugin = "org.metaborg.gradle.config.devenv-settings")

// Include builds from subdirectories, but only if it is from an included repository.
configure<DevenvSettingsExtension> {
  // Manually include nested composite builds, as IntelliJ does not support them.
  if(repoProperties["coronium"]?.include == true && rootDir.resolve("coronium").exists()) {
    includeBuild("coronium/plugin")
    includeBuild("coronium/example")
  }
  if(repoProperties["spoofax.gradle"]?.include == true && rootDir.resolve("spoofax.gradle").exists()) {
    includeBuild("spoofax.gradle/plugin")
    includeBuild("spoofax.gradle/example")
  }

  // HACK: include rest of the builds AFTER including the Gradle plugins, because included build order matters.
  includeBuildsFromSubDirs(true)

  if(repoProperties["pie"]?.include == true && rootDir.resolve("pie").exists()) {
    includeBuild("pie/core")
    includeBuild("pie/lang")
  }

  if(repoProperties["spoofax-pie"]?.include == true && rootDir.resolve("spoofax.pie").exists()) {
    includeBuild("spoofax.pie/core")
    includeBuild("spoofax.pie/example")
  }
}
