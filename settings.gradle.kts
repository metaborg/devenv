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
// Manually include nested composite builds, as IntelliJ does not support them.
configure<DevenvSettingsExtension> {
  if(repoProperties["coronium"]?.include == true && rootDir.resolve("coronium").exists()) {
    includeBuildWithName("coronium", "coronium.root")
    includeBuildWithName("coronium/plugin", "coronium")
    includeBuildWithName("coronium/example", "coronium.example")
  }

  if(repoProperties["releng"]?.include == true && rootDir.resolve("releng").exists()) {
    includeBuildWithName("releng/gradle/java", "spoofax2.releng.java.root")
    includeBuildWithName("releng/gradle/language", "spoofax2.releng.language.root")
  }

  // HACK: include rest of the builds AFTER including the Gradle plugins, because included build order matters.
  includeBuildsFromSubDirs(true)

  if(repoProperties["pie"]?.include == true && rootDir.resolve("pie").exists()) {
    includeBuildWithName("pie/core", "pie.core.root")
    includeBuildWithName("pie/lang", "pie.lang.root")
    includeBuildWithName("pie/bench", "pie.bench")
  }
  if(repoProperties["spoofax-pie"]?.include == true && rootDir.resolve("spoofax.pie").exists()) {
    includeBuildWithName("spoofax.pie", "spoofax3.root")
    includeBuildWithName("spoofax.pie/core", "spoofax3.core.root")
    includeBuildWithName("spoofax.pie/lwb", "spoofax3.lwb.root")
    includeBuildWithName("spoofax.pie/example", "spoofax3.example.root")
  }
}

fun includeBuildWithName(dir: String, name: String) {
  includeBuild(dir) {
    try {
      ConfigurableIncludedBuild::class.java
        .getDeclaredMethod("setName", String::class.java)
        .invoke(this, name)
    } catch(e: NoSuchMethodException) {
      // Running Gradle < 6, no need to set the name, ignore.
    }
  }
}
