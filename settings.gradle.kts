rootProject.name = "devenv"

// This allows us to use plugins from Metaborg Artifacts
pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
}

// This allows us to use the catalog in dependencies
dependencyResolutionManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
    versionCatalogs {
        create("libs") {
            from("org.metaborg.spoofax3:catalog:0.3.3")
        }
    }
}

plugins {
    // This downloads an appropriate JVM if not already available
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("com.gradle.enterprise") version ("3.17.3")
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
        includeBuild("gitonium")
    }
    if (isRepositoryIncluded("coronium")) {
        includeBuild("coronium") { name = "coronium.root" }
    }

    // Dependency management
    if (isRepositoryIncluded("depman")) {
        includeBuild("depman")
    }

    // Independent common Java libraries.
    if (isRepositoryIncluded("depman")) {
        includeBuild("depman")
    }
    if (isRepositoryIncluded("log")) {
        includeBuild("log") { name = "log.root" }
    }
    if (isRepositoryIncluded("resource")) {
        includeBuild("resource") { name = "resource.root" }
    }
    if (isRepositoryIncluded("common")) {
        includeBuild("common") { name = "common.root" }
    }

    // PIE Java libraries.
    if (isRepositoryIncluded("pie")) {
        includeBuild("pie") { name = "pie.root" }
    }

    // Spoofax 2 Java libraries, languages, and Gradle plugin.
    if (isRepositoryIncluded("releng")) {
        includeBuild("releng/gradle") { name = "spoofax2.releng.root" }
    }

    // PIE DSL (include after Spoofax 2, since it uses the Spoofax 2 Gradle plugin)
    if (isRepositoryIncluded("pie")) {
        includeBuild("pie/lang") { name = "pie.lang.root" }
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
