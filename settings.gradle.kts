import mb.gradle.config.devenv.DevenvSettingsExtension

rootProject.name = "devenv"

// Make plugin repositories available, for loading plugins in included builds.
pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

enableFeaturePreview("GRADLE_METADATA")

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
  includeBuildsFromSubDirs(true)
  // Manually include nested composite builds, as IntelliJ does not support them.
  if(repoProperties["spoofax.gradle"]?.include == true && rootDir.resolve("spoofax.gradle").exists()) {
    includeBuild("spoofax.gradle/plugin")
    includeBuild("spoofax.gradle/example")
  }
  if(repoProperties["spoofax-pie"]?.include == true && rootDir.resolve("spoofax.pie").exists()) {
    includeBuild("spoofax.pie/core")
    includeBuild("spoofax.pie/example")
  }
}
