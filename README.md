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
However, to build on the command-line, Gradle does not need to be installed, as this repository includes a Gradle wrapper script (`gradlew`/`gradlew.bat`) which automatically downloads and runs Gradle 6.8.

If you plan to import this project into IntelliJ, you do need to install Gradle.
On macOS/Linux, we recommend installing Gradle 6.8 using the [SDKMAN!](https://sdkman.io/) package manager with `sdk install gradle 6.8`.
On Windows, we recommend [Chocolatey](https://chocolatey.org/) with `choco install gradle --version=6.8`.
In case you use Gradle 6.8 in IntelliJ, use the `gradle` command instead of `./gradlew` to ensure that command-line builds use the same Gradle version and cache.

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

Because we use a mix of composite and multi-project builds, additional steps are required to traverse the hierarchy to find and run tasks in them.
To [run tasks of a subproject in an included composite build, use the `:included-build-name:subproject-name:task-name` syntax](https://docs.gradle.org/current/userguide/composite_builds.html#composite_build_executing_tasks).
For example, to run the `test` task in subproject `calc` of included composite build `spoofax3.example.root`:
```shell script
./gradlew :spoofax3.example.root:calc:test
```

So, to explore the available tasks, 1) look at the available composite builds, 2) look at the subprojects of a composite build, 3) run the `tasks` task there.
This goes as follows:
1) To get a list of all included composite builds, run:
```shell script
./gradlew includedBuilds
```

2) To get a list of subprojects of an included composite build, use the `:included-build-name:projects` syntax. For example:
```shell script
./gradlew :spoofax3.example.root:projects
```

3) Run the `tasks` task:
```shell script
./gradlew :spoofax3.example.root:calc:tasks
```

This covers exploring and running tasks from the command-line, but IDEs have dedicated GUIs for doing this in a more visual way.
For example, in IntelliJ, the Gradle tool window shows the entire composite build, subproject, and task hierarchy, making exploration easy.

## Importing into IntelliJ IDEA

[Import the project as a Gradle project](https://www.jetbrains.com/help/idea/gradle.html#gradle_import_project_start).
In the wizard, choose _Use Gradle from_: _'Specified location_, and choose the location where Gradle is installed.
With SDKMAN! this would be: `~/.sdkman/candidates/gradle/6.8`, and with Chocolatey: `C:/ProgramData/chocolatey/lib/gradle/tools/gradle-6.8`.
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
On the `Import Options` page, select `Specific Gradle version` and choose `6.8`.

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
Spoofax 2 repositories (such as `sdf` and `nabl`) are available in devenv, but their projects are included via the `releng` repository (similar to how it is done with Maven in `spoofax-releng`).
Java projects are included by `releng/gradle/java/settings.gradle.kts`, whereas Spoofax 2 language projects are included by `releng/gradle/language/settings.gradle.kts`.
These projects are only included if their corresponding repository is set to `update` in the `repo.properties` file of this repository.

### Orthogonal release of Spoofax 2 artifacts

We publish orthogonal releases of most Spoofax 2 artifacts under the `org.metaborg.devenv` group ID so that we can easily modify Spoofax 2 projects while still being able to easily publish a new version of Spoofax 3.
It is important to depend on projects using the `org.metaborg.devenv` group ID, to ensure that the dependency points to a project in devenv.
Furthermore, we also publish an orthogonal version of the Spoofax 2 Gradle plugin with plugin IDs modified to include `.devenv`, such as `org.metaborg.devenv.spoofax.gradle.langspec`.
It is also important to use these plugins, as they are built together with the other `org.metaborg.devenv` artifacts.

These orthogonal versions are published via the `releng` repository.
The version of these orthogonal artifacts that we depend on is controlled by the value of `systemProp.spoofax2DevenvVersion` in `gradle.properties`.

### Regular Spoofax 2 artifacts

However, not all of Spoofax 2's repositories and projects are included in devenv.
Therefore, we also need to depend on artifacts from Spoofax 2.
The version of the Spoofax 2 artifacts that we depend on is controlled by the value of `systemProp.spoofax2Version` in `gradle.properties`.

The Spoofax 2 version can be set to a `SNAPSHOT`, but this should be avoided at all cost, because:
* Publishing the artifacts of a repository requires all dependencies to have non-SNAPSHOT versions.
* The projects in devenv need to be compatible with the chosen version of Spoofax 2, and SNAPSHOTs can change at any time.

> _Note_: Gradle only checks for new SNAPSHOT versions once a day. To force download new SNAPSHOTs the command line, use the Gradle `--refresh-dependencies` command line option. For example:
>
>     ./gradlew buildAll --refresh-dependencies
>
> To force this in IntelliJ, right-click the project in the Gradle panel and choose _Refresh Gradle Dependencies_. If you've already refreshed the dependencies on the command line, simply reimport the Gradle projects if IntelliJ doesn't see the new dependencies.


## Shared and personal development environments

To create a shared development environment, create a new branch of this repository and set up the `repo.properties` file to update the corresponding repositories, and push the branch.

To create a personal development environment, fork this repository and set up the `repo.properties` file in your fork.

In both cases, pull in changes from `develop` of `origin` to receive updates to the build script and list of repositories.


## Behind the scenes

This project uses the [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) for build scripts, so that we can write `settings.gradle.kts` and `build.gradle.kts` in Kotlin, to get more type checking and editor services.

We use a Gradle feature called [Composite Builds](https://docs.gradle.org/current/userguide/composite_builds.html), which allow multiple Gradle builds to be easily composed together.
We include all subdirectories (which are usually repositories) in the composite build, achieved in the last code block in `settings.gradle.kts`.

The repository functionality comes from the `org.metaborg.gradle.config.devenv-settings` and `org.metaborg.gradle.config.devenv-repositories` plugin which are applied at the top of `settings.gradle.kts`/`build.gradle.kts`.
This plugin exposes the `devenv` extension which allows configuration of repositories.


## Troubleshooting
In general, ensure you're calling `./repo` and `./gradlew` on Linux and MacOS (or `repo.bat` and `gradlew.bat` on Windows) instead of your local Gradle installation. The local one may be too old or too new.

### Cannot debug in IntelliJ
Debugging in IntelliJ is a bit buggy at the moment. To force debugging, add the following environment variable to your run configuration:

    JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005

Then start the run configuration in debug mode, and wait until the following shows up in the console:

    Listening for transport dt_socket at address: 5005 Attach debugger

Then click the `Attach debugger` text in the console to attach the debugger and start debugging.

Note that this enables debugging for any Gradle task that executes Java in an isolated way, including any (Java/Kotlin) compilation tasks that run in a separate process.
Make sure to first build normally such that these tasks are no longer executed, then run your debugging configuration.

If you are debugging tests, make sure that the test results are cleaned before by running `cleanTest`, otherwise Gradle may skip the test task. For example, run the following Gradle tasks as part of the run configuration:

    :spoofax3.lwb.root:spoofax.dynamicloading:cleanTest :spoofax3.lwb.root:spoofax.dynamicloading:test

### Profiling in IntelliJ
Profiling in IntelliJ can be done similarly to debugging. For example, to profile with YourKit, add the following environment variable to your run configuration:

    JAVA_TOOL_OPTIONS=-agentpath:/Applications/YourKit-Java-Profiler-2020.9.app/Contents/Resources/bin/mac/libyjpagent.dylib=listen=all,sampling,onexit=snapshot

If you are using a different profiler, the `agentpath` needs to point to the corresponding agent of your profiler, and the settings after the agent will need to be tailored towards your profiler.
In the example above, the YourKit profiler will attach to the program, enable CPU sampling, and create a snapshot when the program ends.
The snapshot can then be opened and inspected in YourKit.

Similar to debugging, this enables profiling for any Gradle task that executes Java in an isolated way, and tests must be cleaned before profiling to force tests to be executed.

### Spoofax 2 language fails to build with "Previous build failed and no change in the build input has been observed"

If building a Spoofax 2 language fails due to some ephemeral issue, or if building is cancelled (because you cancelled the Gradle build), the following exception may be thrown during the build:

    org.metaborg.core.MetaborgException: Previous build failed and no change in the build input has been observed, not rebuilding. Fix the problem, or clean and rebuild the project to force a rebuild

This is an artefact of the Pluto build system refusing to rebuild if it failed but no changes to the input were detected.
To force Pluto to rebuild, delete the `target/pluto` directory of the language.

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
