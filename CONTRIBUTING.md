# 🌱 Contributing

First of all, we would like to thank you for your time and efforts on this project, we appreciate it

You can see tutorials online on how to contribute to any open-source project, it's a simple process, and you can do it
even if you are not Git expert, simply start by forking the repository, cloning it, creating a new branch, make your
changes
and commit them, then push the branch to your fork, and you will get a link to send a PR to the upstream repository

If you don't have anything specific in mind to improve or fix, you can take a look at
the [issues](https://github.com/ellet0/kraft-sync/issues) or take a look at
the todos of the project, they all start with `TODO:` so you can search in your IDE or use the todos tab of the IDE

> We highly recommend contacting us if you're planning to make big changes.

## 📋 Requirements

[//]: # (This section is referenced by the README.md file)

Make sure you installed the following:

1. JDK (Java Development Kit) 11
   you can use your favorite distro,
   we suggest [Adoptium](https://adoptium.net/) or [Amazon Corretto](https://aws.amazon.com/corretto/).

   Adding Java to your system path would be useful to run Gradle tasks directly in the command
   line.

   > The IDE needs to be configured to use JDK 11 for this project

2. Your favorite IDE, we suggest [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/)
   you can get it directly from the installer or by [Jetbrains Toolbox](https://www.jetbrains.com/toolbox-app/)
3. [KtLint Intellij IDEA Plugin](https://plugins.jetbrains.com/plugin/15057-ktlint) (optional) which is an
   anti-bike-shedding linter/formatter for Kotlin code based on the Kotlin Coding Conventions, Kotlin Style
   guide, and other best practices.

## 🧪 Test your changes

Make sure you have the [Requirement](#-requirements) installed and configured correctly

To set up your IDE for this project, so you can test the changes:

1. Run `./gradlew build` or the build task for the first time
2. To run the script:

<details>
<summary>IntelliJ IDEA Community Edition</summary>
Use one of the shared run configurations of IntelliJ IDEA in `.idea` which will be available in:

![IntelliJ IDEA Run Configurations](https://github.com/ellet0/kraft-sync/assets/73608287/e852c5c7-2133-4c96-95b4-2daa75f5464e)

<details>
<summary>Or if you want to add custom run configurations</summary>

1. Edit the Run/Debug configurations of IntelliJ IDEA, click on the add plus
2. Choose the JAR Application, name it, and choose the JAR file path which is usually located under
   the [dist](./dist),
   also, change the working directory to a directory other than the current or somewhere that is in `.gitignore` like
   like `testScript`
3. In before launch add two Gradle tasks, first `clean` and then `shadowJar` (in order) or only `shadowJar`.
   You can create multiple tasks depending on the use-case
4. You can now use the new run configuration.
   You can also create and test other configurations for the GUI and non-GUI
   modes.

</details>

</details>

<details>
<summary>Others</summary>

If you're not using any of the supported IDEs, text editor or something like `vim` and you want to test the changes,
and you want to test the code changes, you can use the following Gradle tasks:

- `./gradlew build` to build the project, running checks, lints, tests and build JAR files
- `./gradlew run` to run the application
- `./gradlew shadowJar` to build the uber JAR file
- `./gradlew runJar` to run the application using the uber JAR file in GUI mode
- `./gradlew runJarCli` to run the application using the uber JAR file in non-GUI mode
- `./gradlew minimizedJar` to build the minimized JAR file.
- `./gradlew runMinimizedJar` to run the application using the minimized JAR file in GUI mode
- `./gradlew runMinimizedJarCli` to run the application using the minimized JAR file in non-GUI mode
- `./gradlew obfuscatedJar` to build the obfuscated JAR file.
- `./gradlew runObfuscatedJar` to run the application using the obfuscated JAR file in GUI mode
- `./gradlew runObfuscatedJarCli` to run the application using the obfuscated JAR file in non-GUI mode

</details>

If you need to change the working directory for the script, for example, when testing a feature that is specific to a
launcher, then you will need to manually create a run configuration like we
discussed above

> The sync script might ask you the url for the sync info where it will send the GET request.<br>
> Consider uploading the file on your local machine on `localhost` server
> to speed up the development and lower the resource cost.
>
> The same applies for the mods, resource-packs, etc...
>
> Use [dev-local-server](dev-local-server/README.md) module

> If you're on **Microsoft Windows**, replace `gradlew` with `gradlew.bat`.

## 📝 Guidelines

1. **Code Style and Formatting**:

   Adhere to the Kotlin Coding Conventions (https://kotlinlang.org/docs/coding-conventions.html).
   Use consistent naming conventions for variables, functions, classes, etc.
   Follow a consistent code formatting style throughout the project.

   We use [KtLint](https://pinterest.github.io/ktlint/latest/) by using
   [KtLint Gradle](https://github.com/JLLeitschuh/ktlint-gradle) to make the process easier.
2. **Documentation**:

   Document public APIs using KotlinDoc comments.
   Provide comprehensive documentation for any complex algorithms, data structures, or significant functionality.
   Write clear and concise commit messages and pull request descriptions.
3. **Performance**:

   Write efficient code and avoid unnecessary overhead.
   Profile the application for performance bottlenecks and optimize critical sections if needed.
4. **Bundle size**:

   Try to make the JAR file size as less as possible and as much as needed
5. **Code Review**:

   Encourage code reviews for all changes to maintain code quality and catch potential issues early.
   Use pull requests and code reviews to discuss proposed changes and improvements.
6. **Versioning and Releases**:

   The project uses standard practices for versioning and releases:
    - **Versioning**: [Semantic Versioning](https://semver.org/) which use `MAJOR.MINOR.PATCH` format to indicate
      changes.
    - **Commit messages**: [Conventional Commits](https://www.conventionalcommits.org/) for clear and consistent commit
      messages.
    - **Pull Request Titles**: [PR title using conventional commits](https://flank.github.io/flank/pr_titles/) style for
      clarity and consistency.
    - **Changelog**: [Keep a Changelog](https://keepachangelog.com/) to document release notes and changes.

   Clearly document release notes and changes for each version.

   We might introduce breaking changes even in non-major versions. We plan to avoid doing this someday.
7. **Consistency**:

   Adhere to a consistent coding style throughout the project to improve readability and maintainability
8. **Meaningful Names**:

   Use descriptive variable, class, and function names that clearly convey their purpose.
9. **Testing**:

   This project does not prioritize testing rigorously, typically featuring unit tests.

## ⚙️ Development Notes

- If you add a new dependency, update existing one, add assets in the resources or do anything that increase or decrease
  the size of the bundle, make sure to update the badges in `README.md` (at the start) to update the size
- The changes of `CHANGELOG.md` and the version will usually be automated.
- The version of the libraries and the tools are usually in [gradle/libs.versions.toml](./gradle/libs.versions.toml),
  except some tools like Gradle wrapper which is in
  [gradle/wrapper/gradle-wrapper.properties](./gradle/wrapper/gradle-wrapper.properties) and can be updated using
  `./gradlew wrapper --gradle-version=<new-version-here>`
- Run the following command: `./gradlew ktCheck`
  or `./gradlew build` which will usually run `ktlint` tasks.
  If possible, try to resolve any warnings that appear.
  This helps ensure that the codebase stays clean and consistent
- When doing any code changes, use the Kotlin Run configuration to test them, if possible, also try to run the JAR
  using the run configurations that are provided as in `.idea` and as a Gradle task to make it work with other IDEs and
  text editor, as a bonus, you could also test the minimized JAR and modify the Proguard rules and configurations if
  necessary.
- Usually when we add a new fields or modify existing ones in the data classes, like, for example, adding `description`
  field in the `Mod` data class, we will try to update the [Admin](./admin) module too to convert the new
  data from other launchers to make the process easier for administrations
- The project generate `BuildConfig` object using a Gradle task once you start the application or building it,
  you might get `Unresolved reference: BuildConfig` which can be solved by either start the application or building it.

### 🚧 Development Known Issues

- If you work on different modules,
    ```kotlin
    // module1/Utils.kt
    
    fun onePlusOne() = 1 + 1
    ```

    ```kotlin
    // module2/Utils.kt
    
    fun onePlusTwo() = 1 + 2
    ```
  Assuming `module1` depends on `module2` and use classes and functions from `module2`
  Then this would cause runtime exception: `Exception in thread "main" java.lang.NoSuchMethodError`

  When calling `onePlusTwo()` from `module2` in `module1`
  this is not necessarily a bug, and it's related to how Java 9 modules work;
  the Kotlin compiler won't give you compile error instead it will cause runtime error `java.lang.NoSuchMethodError`,
  a workaround would be to double-check to use different package or file name,
  for
  more [details](https://youtrack.jetbrains.com/issue/KT-64744/NoSuchMethodError-on-some-but-not-all-methods-from-another-Gradle-module).

  The issue will be caused once running the application, which is a good thing
  to catch the error earlier rather than having it affecting some functionalities.