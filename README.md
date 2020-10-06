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
* 'Devenv tasks': tasks for managing this development environment, such as `repoStatus`.

To run tasks in composite builds, use the `runTasksInCompositeBuild` task with the `compositeBuildName` specifying the name of the composite build (e.g., `spoofax3.lwb.root`), and `taskPaths` specifying a semicolon-separated list of Gradle tasks to execute (e.g. `:stratego.spoofax:cleanTest;:stratego.spoofax:test`).
For example, to test the Stratego language in Spoofax 3, run:

```shell script
./gradlew runTasksInCompositeBuild -PtaskPaths=:stratego.spoofax:cleanTest;:stratego.spoofax:test -PcompositeBuildName=spoofax3.lwb.root
```


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


## Modifying repositories

By default, no repositories will be cloned or updated, they must be explicitly included.
List all available repositories and their properties with:

```shell script
gradlew repoList
```

Each repository has the following properties:
* `<name of the repository>`: whether the repository will be enabled/included in the build. The name of the repository must be defined in `build.gradle.kts`.
* `update`: each repository for which `update` is `true` will be cloned or updated when running `gradlew updateRepos`.
* `branch`: repository will be checked out to the corresponding `branch`, which defaults to the current branch of this repository.
* `dir`: repository will be cloned into the corresponding `dir`, which defaults to the `name`. Changing this property will cause a new repository to be cloned, while the old repository is left untouched (in case you have made changes to it), which may cause conflicts. In that case, push your changes and delete the old repository.
* `url`: The remote `url` will be used for cloning and pulling, which defaults to `<repoUrlPrefix>/<name>.git` where the `repoUrlPrefix` is set in `build.gradle.kts`.

To enable/include a repository, create or open the `repo.properties` file, and add a `<name of the repository>=true` line to it. For example:

```properties
spoofax.pie=true
```

The `branch`, `dir`, and `url` properties can be overridden in `repo.properties` by appending the name of the property to the key, for example:

```properties
spoofax.pie.branch=develop
spoofax.pie.path=spoofax3
spoofax.pie.url=https://github.com/metaborg/spoofax.pie.git
```

## Adding repositories

To add a new repository, add a `registerRepo` call to the first `devenv` block in `build.gradle.kts`.
The first argument is the name of the repository, which must be unique.
Default values for `update`, `branch`, `dir`, and `url` can be given as optional arguments.


## Adding new Gradle tasks

In the first `tasks` block in `build.gradle.kts`, `register` a new task that depends on a task in an included build that does what you want.


## Publishing and continuous integration

Devenv is not used for publishing, only for development and builds.
Publishing the artifacts of a repository is done via that repository.

The non-master branches of devenv are [automatically built with our build farm](https://buildfarm.metaborg.org/view/Spoofax-PIE/job/metaborg/job/devenv/).
Whenever a repository that devenv includes/updates has changed, the build for devenv is triggered automatically.


## Working with Spoofax 2 projects

Due to the complicated build and structure of Spoofax 2, working with Spoofax 2 projects requires some special instructions.

Devenv can build several Spoofax 2 projects from source.
Spoofax 2 projects are included via the `releng` repository (similar to how it is done with Maven in `spoofax-releng`).
Java projects are included by `releng/gradle/java/settings.gradle.kts`, whereas Spoofax 2 language projects are included by `releng/gradle/language/settings.gradle.kts`.
These projects are only included if their corresponding repository is set to update in the `repo.properties` file of this repository.

Besides building and using these Spoofax 2 projects from source, we also depend on artifacts of Spoofax 2.
The version of the Spoofax 2 artifacts that we depend on is controlled by the value of `systemProp.spoofax2Version` in `gradle.properties`.

> _Note_: Gradle only checks for new SNAPSHOT versions once a day. To force download new SNAPSHOTs the command line, use the Gradle `--refresh-dependencies` command line option. For example:
>
>     ./gradlew buildAll --refresh-dependencies
>
> To force this in IntelliJ, right-click the project in the Gradle panel and choose _Refresh Gradle Dependencies_. If you've already refreshed the dependencies on the command line, simply reimport the Gradle projects if IntelliJ doesn't see the new dependencies.

Publishing the artifacts of a repository requires all dependencies to have non-SNAPSHOT versions.
Therefore, the Spoofax 2 version should be kept to a non-SNAPSHOT version as much as possible.
Furthermore, artifacts from Spoofax 2 repositories cannot be published via Gradle.
Spoofax 2 is published as a whole via [spoofax-releng with these instructions](http://www.metaborg.org/en/latest/source/dev/release.html).


## Shared and personal development environments

To create a shared development environment, create a new branch of this repository and set up the `repo.properties` file to update the corresponding repositories, and push the branch.

To create a personal development environment, fork this repository and set up the `repo.properties` file in your fork.

In both cases, pull in changes from `master` of `origin` to receive updates to the build script and list of repositories.


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

### Unknown command-line option '--args'
Command-line arguments such as `--args` are not supported for tasks in the root project, such as the `runSdf3Cli` task. Instead, go to the relevant included build and call the task directly.

    cd spoofax.pie/example
    ./gradlew :sdf3.cli:run --args="-V"

The working directory is the directory with the `gradle.build.kts` file of the CLI project. This cannot be changed. For example, `spoofax.pie/example/sdf3/sdf3.cli/` for the `:sdf3.cli` project.
