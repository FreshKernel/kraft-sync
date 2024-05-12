# Contributing

First of all, we would like to thank you for your time and efforts on this project, we appreciate it

You can see tutorials online on how to contribute to any open source project, it's a simple process, and you can do it
even if you are not Git expert, simply start by forking the repository, clone it, creating a new branch, make your
changes
and commit them, then push the branch to your fork, and you will get link to send a PR to the upstream repository

If you don't have anything specific in mind to improve or fix, you can take a look at the issues tab or take a look at
the
todos of the project, they all start with `TODO: ` so you can search in your IDE or use the todos tab in the IDE

## Requirements

[//]: # (This section is referenced by the README.md file)

Make sure you installed the followings:

1. [Intellij IDEA Community Edition](https://www.jetbrains.com/idea/download/) directly from the website or
   by [Jetbrains Toolbox](https://www.jetbrains.com/toolbox-app/)
2. Java JDK 11, you can get the OpenJDK from Eclipse
   from [here](https://adoptium.net/temurin/releases/?version=11&package=jdk) and make sure to add it to your path
3. [KtLint Intellij IDEA Plugin](https://plugins.jetbrains.com/plugin/15057-ktlint) (optional) which is an
   anti bike-shedding linter/formatter for Kotlin code based on the Kotlin Coding Conventions, Kotlin Style
   guide, and other best practices.

## Test your changes 🧪

Make sure you have the [Requirement](#requirements) installed and configured correctly

To set up your IDE for this project, so you can test the changes:

1. Run `./gradlew build` or the build task for the first time
2. Edit the Run/Debug configurations of Intellij IDEA, click on the add plus
3. Choose JAR Application, name it, choose the jar path which is usually location under the [build/libs](./build/libs),
   also change
   the working directory to a directory other than the current or somewhere that is in `.gitignore` like the (data)
   folder
4. In the before launch add two gradle tasks, first `clean` and then `shadowJar` (in order) or only `shadowJar` or you
   can create multiple tasks (CleanAndRun, Run) depending on the use case,
5. You can now use the new run configuration
6. You can also create and test other configurations for the GUI and non GUI mode to make sure it's working properly

## Guidelines 📝

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

   Try to make the jar file size as less as possible but as much as needed
5. **Code Review**:

   Encourage code reviews for all changes to maintain code quality and catch potential issues early.
   Use pull requests and code reviews to discuss proposed changes and improvements.
6. **Versioning and Releases**:

   Try to follow semantic versioning for releases (https://semver.org/) when possible.
   Clearly document release notes and changes for each version.
   Please notice for now we might introduce breaking changes in non-major version but will always provide migration
   guide in each release info
7. **Consistency**:

   Adhere to a consistent coding style throughout the project for improves readability and maintainability
8. **Meaningful Names**:

   Use descriptive variable, class, and function names that clearly convey their purpose.
9. **Testing**:

   For now, we won't focus on testing much but try to write tests when possible