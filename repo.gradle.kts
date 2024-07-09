buildscript {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }
    dependencies {
        classpath("org.metaborg:gradle.config:0.7.1")
    }
}
apply(plugin = "org.metaborg.gradle.config.devenv-repositories")

// Auto-accept build scan TOS
extensions.findByName("buildScan")?.withGroovyBuilder {
    try {
        // New Developcity plugin
        setProperty("termsOfUseUrl", "https://gradle.com/help/legal-terms-of-use")
        setProperty("termsOfUseAgree", "yes")
    } catch (ex: groovy.lang.MissingPropertyException) {
        // Deprecated Gradle Enterprise plugin
        setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
        setProperty("termsOfServiceAgree", "yes")
    }
}
