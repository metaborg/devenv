# MetaBorg development environment

This repository contains a Gradle script to manage a development environment for MetaBorg projects.
The script supports cloning and updating Git repositories that contain the source code for MetaBorg projects, and for building (compiling and testing) these projects.


## Requirements

### JDK

To run Gradle and build this repository, a Java Development Kit (JDK) is needed.
JDK versions between 8 and 11 are supported. Higher versions may work, but have not been tested yet.

We recommend to [install JDK11 from AdoptOpenJDK](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot), or to use your favourite package manager (e.g., `brew install adoptopenjdk11` on macOS, `choco install adoptopenjdk11` on Windows).

If you require JDK8 for compatibility reasons, [install JDK8 from AdoptOpenJDK](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot), or to use your favourite package manager (e.g., `brew install adoptopenjdk8` on macOS, `choco install adoptopenjdk8` on Windows).

### Gradle

Gradle is the build system we use to build devenv.
Most Gradle versions between 5.6.4 and 6.7.1 should be supported, although we currently test with Gradle 5.6.4 and 6.7.1.
However, to build on the command-line, Gradle does not need to be installed, as this repository includes a Gradle wrapper script (`gradlew`/`gradlew.bat`) which automatically downloads and runs Gradle 5.6.4.

If you plan to import this project into IntelliJ, you do need to install Gradle.
On macOS/Linux, we recommend installing Gradle 6.7.1 using the [SDKMAN!](https://sdkman.io/) package manager with `sdk install gradle 6.7.1`.
On Windows, we recommend [Chocolatey](https://chocolatey.org/) with `choco install gradle --version=6.7.1`.
In case you use Gradle 6.7.1 in IntelliJ, use the `gradle` command instead of `./gradlew` to ensure that command-line builds use the same Gradle version and cache.


## Updating repositories

Repositories are updated with the `repo` script.
To update repositories to their latest version, and to clone new repositories, run:

```shell script
./repo update
```

On Windows, use `repo.bat` instead.
For a list of other operations possible on repositories, run:

```shell script
./repo tasks
```

and look for tasks under group "Devenv repository tasks".


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
to get an overview of which tasks can be executed.

To run tasks in composite builds, use the `runTasksInCompositeBuild` task with the `compositeBuildName` specifying the name of the composite build, and `taskPaths` specifying a semicolon-separated list of Gradle tasks to execute.
For example, to test the Stratego language in Spoofax 3, run:

```shell script
./gradlew runTasksInCompositeBuild -PtaskPaths=:stratego.spoofax:cleanTest;:stratego.spoofax:test -PcompositeBuildName=spoofax3.lwb.root
```


## Importing into IntelliJ IDEA

[Import the project as a Gradle project](https://www.jetbrains.com/help/idea/gradle.html#gradle_import_project_start).
In the wizard, choose _Use Gradle from_: _'Specified location_, and choose the location where Gradle is installed.
With SDKMAN! this would be: `~/.sdkman/candidates/gradle/6.7.1`, and with Chocolatey: `C:/ProgramData/chocolatey/lib/gradle/tools/gradle-6.7.1`.
Also ensure that _Build and run using_ and _Run tests using_ are both set to _Gradle (default)_.
If the wizard does not show these settings, go to the [Gradle Settings](https://www.jetbrains.com/help/idea/gradle-settings.html) to configure these settings.

When new repositories are cloned, [re-import a linked Gradle project](https://www.jetbrains.com/help/idea/work-with-gradle-projects.html#gradle_refresh_project).

To run Gradle tasks inside IntelliJ, see [Run Gradle tasks](https://www.jetbrains.com/help/idea/work-with-gradle-tasks.html#gradle_tasks).
Similarly, for testing, see [Testing in Gradle](https://www.jetbrains.com/help/idea/work-with-tests-in-gradle.html).
Gradle tasks and tests can be executed in Debug mode, which also enables debugging of any VMs that Gradle starts, such as those for running an application or testing, enabling debugging of applications and tests.

If files in a repository are marked as ignored, add that repository as a version control root. See [Associate a directory with a version control system](https://www.jetbrains.com/help/idea/enabling-version-control.html#associate_directory_with_VCS) for more info.


## Importing into Eclipse

Eclipse supports Gradle through the [buildship](https://projects.eclipse.org/projects/tools.buildship) plugin, which should be installed into Eclipse by default.
However, using Eclipse is discouraged, as IntelliJ has much better support for Gradle.

Import the project as an existing Gradle project. See [Import an existing Gradle project](http://www.vogella.com/tutorials/EclipseGradle/article.html#import-an-existing-gradle-project).
On the `Import Options` page, select `Specific Gradle version` and choose `6.7.1`.

When new repositories are cloned, refresh the `devenv` Gradle project. See [Refresh Gradle Project](http://www.vogella.com/tutorials/EclipseGradle/article.html#updating-classpath-with-the-latest-changes-in-the-build-file).


## Modifying repositories

By default, no repositories will be cloned or updated, they must be explicitly included.
List all available repositories and their properties with:

```shell script
./repo list
```

Each repository has the following properties:
* `<name>`: defines the name of the repository, and whether the repository will be included in the build.
* `<name>.update`: each repository for which `update` is `true` will be cloned or updated when running `./repo update`.
* `<name>.branch`: repository will be checked out to the corresponding `branch`, which defaults to the current branch of this repository.
* `<name>.dir`: repository will be cloned into the corresponding `dir`, which defaults to `<name>`. Changing this property will cause a new repository to be cloned, while the old repository is left untouched (in case you have made changes to it), which may cause conflicts. In that case, push your changes and delete the old repository.
* `<name>.url`: The remote `url` will be used for cloning and pulling, which defaults to `<urlPrefix>/<name>.git`.

To enable/include a repository, create or open the `repo.properties` file, and add a `<name>=true` line to it. For example:

```properties
spoofax-pie=true
```

The `branch`, `dir`, and `url` properties can be overridden in `repo.properties` by appending the name of the property to the key, for example:

```properties
spoofax-pie.branch=develop
spoofax-pie.dir=spoofax3
spoofax-pie.url=https://github.com/Gohla/spoofax.pie.git
```


## Adding new Gradle tasks

Use `tasksWithIncludedBuild` to register tasks from included builds, or use the regular Gradle way of registering tasks with `register` in a `tasks` block.


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

The repository functionality comes from the `org.metaborg.gradle.config.devenv-settings` and `org.metaborg.gradle.config.devenv-repositories` plugin which are applied at the top of `settings.gradle.kts`/`build.gradle.kts`.
This plugin exposes the `devenv` extension which allows configuration of repositories.


## Troubleshooting
In general, ensure you're calling `./repo` and `./gradlew` on Linux and MacOS (or `repo.bat` and `gradlew.bat` on Windows) instead of your local Gradle installation. The local one may be too old or too new.

### Task 'buildAll' not found in root project 'devenv'
You have 'configure on demand' enabled, such as `org.gradle.configureondemand=true` in your `~/.gradle/gradle.properties` file. Disable this.

### Expiring Daemon because JVM heap space is exhausted
The memory limits in `gradle.properties` may be too low, and may need to be increased.
Running the build without `--parallel` may decrease memory pressure, as less tasks are executed concurrently.
Or, there is a memory leak in the build: please make a heap dump and send this to the developers so it can be addressed.

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
