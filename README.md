# MetaBorg development environment

This repository contains a Gradle script to manage a development environment for MetaBorg projects.
The script supports cloning and updating Git repositories that contain the source code for MetaBorg projects, building (compiling and testing) these projects, and publishing of their artifacts.

## Requirements

### JDK 8

To run Gradle and build this repository, a Java Development Kit (JDK) of version 8 is needed.
Higher versions of the JDK (9+) are currently not supported, as not all our Java code is compatible with Java 9+ yet.

We recommend to [install JDK8 from AdoptOpenJDK](https://adoptopenjdk.net/), or to use your favourite package manager (e.g., `brew install adoptopenjdk8` on macOS, `choco install adoptopenjdk8` on Windows).

### Gradle

#### Installing Gradle 5.6.4

Gradle version 5.6.4 (exactly) is needed.
However, to build on the command-line, Gradle does not need to be installed, as this repository includes a Gradle wrapper script (`gradlew`/`gradlew.bat`) which automatically downloads and runs Gradle 5.6.4.

If you plan to import this project into IntelliJ, you do need to install Gradle 5.6.4.
On macOS/Linux, we recommend installing Gradle 5.6.4 using the [SDKMAN!](https://sdkman.io/) package manager with `sdk install gradle 5.6.4`.
On Windows, we recommend [Chocolatey](https://chocolatey.org/) with `choco install gradle --version=5.6.4`.

#### Configuring Gradle's memory limits

To configure Gradle's memory limits, modify (or create) the `~/.gradle/gradle.properties` file and add the following:

```properties
org.gradle.jvmargs=-Xmx2G -Xss16M
```

## Updating repositories

To update repositories to their latest version, and to clone new repositories, run:

```shell script
./gradlew repoUpdate
```

On Windows, use `gradlew.bat` instead.

## Building

To build all projects in all repositories, run:

```shell script
./gradlew buildAll
```

## Gradle tasks

Gradle can execute tasks besides just building. Run:

```shell script
./gradlew tasks
```
to get an overview of which tasks can be executed. Interesting tasks will be in these categories:

* 'Composite build tasks': tasks that will be executed on every project, such as `buildAll`.
* 'Application tasks': tasks that will build and run applications.
* 'Devenv tasks': tasks for managing this development environment, such as `repoStatus`.


## Importing into IntelliJ IDEA

[Import the project as a Gradle project](https://www.jetbrains.com/help/idea/gradle.html#gradle_import_project_start).
In the wizard, choose _Use Gradle from_: _'Specified location_, and choose the location where Gradle 5.6.4 is installed.
With SDKMAN! this would be: `~/.sdkman/candidates/gradle/5.6.4`, and with Chocolatey: `C:/ProgramData/chocolatey/lib/gradle/tools/gradle-5.6.4`.
Also ensure that _Build and run using_ and _Run tests using_ are both set to _Gradle (default)_.
If the wizard does not show these settings, go to the [Gradle Settings](https://www.jetbrains.com/help/idea/gradle-settings.html) to configure these settings.

When new repositories are cloned, [re-import a linked Gradle project﻿](https://www.jetbrains.com/help/idea/work-with-gradle-projects.html#gradle_refresh_project).

To run Gradle tasks inside IntelliJ, see [Run Gradle tasks](https://www.jetbrains.com/help/idea/work-with-gradle-tasks.html#gradle_tasks).
Similarly, for testing, see [Testing in Gradle](https://www.jetbrains.com/help/idea/work-with-tests-in-gradle.html).
Gradle tasks and tests can be executed in Debug mode, which also enables debugging of any VMs that Gradle starts, such as those for running an application or testing, enabling debugging of applications and tests.

If files in a repository are marked as ignored, add that repository as a version control root. See [Associate a directory with a version control system﻿](https://www.jetbrains.com/help/idea/enabling-version-control.html#associate_directory_with_VCS) for more info.


## Importing into Eclipse

Eclipse supports Gradle through the [buildship](https://projects.eclipse.org/projects/tools.buildship) plugin, which should be installed into Eclipse by default.
However, using Eclipse is discouraged, as IntelliJ has much better support for Gradle.

Import the project as an existing Gradle project. See [Import an existing Gradle project](http://www.vogella.com/tutorials/EclipseGradle/article.html#import-an-existing-gradle-project).
On the `Import Options` page, select `Specific Gradle version` and choose `5.6.4`.

When new repositories are cloned, refresh the `devenv` Gradle project. See [Refresh Gradle Project](http://www.vogella.com/tutorials/EclipseGradle/article.html#updating-classpath-with-the-latest-changes-in-the-build-file).

Running tasks of a composite build in Eclipse is currently not supported.

## Modifying repositories

By default, no repositories will be cloned or updated, they must be explicitly included.
List all available repositories and their properties with:

```shell script
gradlew repoList
```

Each repository has the following properties:
* `name`: name of the repository, defined in `build.gradle.kts`.
* `update`: each repository for which `update` is `true` will be cloned or updated when running `gradlew updateRepos`.
* `branch`: repository will be checked out to the corresponding `branch`, which defaults to the current branch of this repository.
* `path`: repository will be cloned into the corresponding `path`, which defaults to the `name`. Changing this property will cause a new repository to be cloned, while the old repository is left untouched (in case you have made changes to it), which may cause conflicts. In that case, push your changes and delete the old repository.
* `url`: The remote `url` will be used for cloning and pulling, which defaults to `<repoUrlPrefix>/<name>.git` where the `repoUrlPrefix` is set in `build.gradle.kts`.

To enable `update` for a repository, create or open the `repo.properties` file, and add a `<name>=true` line to it. For example:

```properties
spoofax.eclipse=true
```

The `branch`, `path`, and `url` properties can be overridden in `repo.properties` by appending the name of the property to the key, for example:

```properties
spoofax.eclipse.branch=develop
spoofax.eclipse.path=spoofax-eclipse
spoofax.eclipse.url=https://github.com/metaborg/spoofax.eclipse.git
```

## Adding repositories

To add a new repository, add a `registerRepo` call to the first `devenv` block in `build.gradle.kts`.
The first argument is the name of the repository, which must be unique.
Default values for `update`, `branch`, `path`, and `url` can be given as optional arguments.

## Adding new Gradle tasks

In the first `tasks` block in `build.gradle.kts`, `register` a new task that depends on a task in an included build that does what you want.

## Shared and personal development environments

To create a shared development environment, create a new branch of this repository and set up the `repo.properties` file to update the corresponding repositories, and push the branch.

To create a personal development environment, fork this repository and set up the `repo.properties` file in your fork.

In both cases, pull in changes from `master` of `origin` to receive updates to the build script and list of repositories.

## Depending on Spoofax 2 snapshot
Spoofax 3 has some dependencies on specific release versions of Spoofax 2 libraries. If for some reason you need to depend on the latest snapshot version of Spoofax 2 such as `2.6.0-SNAPSHOT`, do the following:

- add the `spoofax.gradle` project [to your development environment](#Shared and personal development environments)
- change the `spoofaxVersion` variable in `spoofax.gradle/plugin/build.gradle.kts` to the desired snapshot version
- change the `spoofax2Version` variable in `spoofax.pie/core/spoofax.depconstraints/build.gradle.kts` to the desired snapshot version

> _Note_: Gradle does not automatically check or download the latest snapshots. To force this on the command line, use the Gradle `--refresh-dependencies` command line option. For example:
>
>     ./gradlew buildAll --refresh-dependencies
>
> To force this in IntelliJ, right-click the project in the Gradle panel and choose _Refresh Gradle Dependencies_. If you've already refreshed the dependencies on the command line, simply reimport the Gradle projects if IntelliJ doesn't see the new dependencies.


## Behind the scenes

This project uses the [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) for build scripts, so that we can write `settings.gradle.kts` and `build.gradle.kts` in Kotlin, to get more type checking and editor services.

We use a Gradle feature called [Composite Builds](https://docs.gradle.org/current/userguide/composite_builds.html), which allow multiple Gradle builds to be easily composed together.
We include all subdirectories (which are usually repositories) in the composite build, achieved in the last code block in `settings.gradle.kts`.

The repository update functionality comes from the `org.metaborg.gradle.config.devenv` plugin which is applied at the top of `build.gradle.kts`.
This plugin exposes the `devenv` extension which allows configuration of repositories.

## Troubleshooting
In general, ensure you're calling `./gradlew` on Linux and MacOS (or `gradlew.bat` on Windows) instead of your local Gradle installation. The local one is most likely too new.

### Task 'buildAll' not found in root project 'devenv'
You have 'configure on demand' enabled, such as `org.gradle.configureondemand=true` in your `~/.gradle/gradle.properties` file. Disable this.

### Expiring Daemon because JVM heap space is exhausted
You didn't set the memory limits found at the start of this README, or they need to be increased even more.

### Could not create service of type FileAccessTimeJournal using GradleUserHomeScopeServices.createFileAccessTimeJournal()
The permissions in your `~/.gradle/` directory are too restrictive. For example, if you're using WSL, ensure the directory is not a symlink to the Windows' `.gradle/` directory.

### Error resolving plugin: Plugin request for plugin already on the classpath must not include a version

> Error resolving plugin [id: 'org.metaborg.gradle.config.devenv', version: '?']
>
> Plugin request for plugin already on the classpath must not include a version

You are not running with the recommended version of Gradle.
