rootProject.name = "devenv"

dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.metaborg.convention.settings") version "0.8.1"
}

// Apply devenv-settings plugin. Settings plugins must still be put on the classpath via a buildscript block.
buildscript {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
    dependencies {
        classpath("org.metaborg:gradle.config:0.7.1")
    }
}
apply(plugin = "org.metaborg.gradle.config.devenv-settings")

// Include builds from subdirectories, but only if it is from an included repository.
// The order of these includes is important. Gradle plugins must be included BEFORE they are used!
// Manually include nested composite builds, as IntelliJ does not support them.
configure<mb.gradle.config.devenv.DevenvSettingsExtension> {
    // Independent Gradle plugins.
    if (isRepositoryIncluded("gradle.config")) {
        includeBuild("gradle.config")
    }
    if (isRepositoryIncluded("gitonium")) {
        includeBuild("gitonium/")
    }
    if (isRepositoryIncluded("coronium")) {
        includeBuild("coronium/") { name = "coronium.root" }
    }

    // Dependency management
    if (isRepositoryIncluded("depman")) {
        includeBuild("depman/")
    }

    // Independent common Java libraries
    if (isRepositoryIncluded("log")) {
        includeBuild("log/")
    }
    if (isRepositoryIncluded("resource")) {
        includeBuild("resource/")
    }
    if (isRepositoryIncluded("common")) {
        includeBuild("common/")
    }

    // Spoofax 2 Java libraries, languages, and Gradle plugin.
    if (isRepositoryIncluded("releng")) {
        includeBuild("releng/gradle") { name = "spoofax2.releng.root" }
    }

    // PIE Java libraries and DSL (include after Spoofax 2, since it uses the Spoofax 2 Gradle plugin)
    if (isRepositoryIncluded("pie")) {
        includeBuild("pie/")
    }


    // Spoofax 3 Java libraries, languages, and Gradle plugins.
    if (isRepositoryIncluded("spoofax-pie")) {
        includeBuild("spoofax.pie") { name = "spoofax3.root" }
    }

    // Jenkins CI
    if (isRepositoryIncluded("jenkins.pipeline")) {
        includeBuild("jenkins.pipeline")
    }
}
