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
    classpath("org.metaborg:gradle.config:0.3.20")
  }
}
apply(plugin = "org.metaborg.gradle.config.devenv-settings")

// Include builds from subdirectories, but only if it is from an included repository.
configure<DevenvSettingsExtension> {
  includeBuildsFromSubDirs(true)
  if(repoProperties["spoofax-pie"]?.include == true && rootDir.resolve("spoofax.pie").exists()) {
    // Manually include Spoofax-PIE (nested) composite builds, as IntelliJ does not support nested composite builds.
    includeBuild("spoofax.pie/core")
    includeBuild("spoofax.pie/example/tiger/spoofaxcore")
    includeBuild("spoofax.pie/example/tiger/generated")
    includeBuild("spoofax.pie/example/tiger/manual")
    includeBuild("spoofax.pie/example/mod")
    includeBuild("spoofax.pie/example/sdf3")
  }
}
