# Control System

Applies control logic to continuous process variables measured in real time.

## Project Setup

### Installing Java

Java 21 must be installed and configured on your machine to build this project:

1. Install [Oracle JDK 21](https://www.oracle.com/ca-en/java/technologies/downloads/#java21) on your machine
2. Add a new system environment variable called `JAVA_HOME` and set it to the installation directory of the JDK
    - Eg. On Windows: `C:\Program Files\Java\jdk-21`
3. Add the binary folder of the JDK to your `PATH` system environment variable
    - Eg. On Windows: `C:\Program Files\Java\jdk-21\bin`

### Building the Project

This project uses Gradle as its build automation tool; the Gradle executable is checked into source control so Gradle installation is not required.
Simply run the following command from the project's root directory to build it and execute all tests:

```angular2html
gradlew build
```

### Settting up SonarLint

This project uses SonarCloud in its CI/CD pipeline to analyze the code in PRs for quality issues.
To ensure that you see (and fix) any flags raised by SonarCloud before your code is in a PR, you must add the SonarLint
plugin to your IDE and bind it to the SonarCloud project. This will ensure that your IDE applies the same linting rules
as the code analysis portion of the CI/CD pipeline.

Follow these steps set up SonarLint on your machine in IntelliJ IDEA:

1. [Log into SonarCloud](https://sonarcloud.io/login) with your GitHub account
2. Within Intellij, Under File -> Settings -> Plugins, search for the SonarLint plugin, install it, and restart IntelliJ
3. A new SonarLint section should now be visible in the bottom left dock of IntelliJ, near the Git icon. Within this
   GUI, click on `Configure SonarLint`
4. Click `Bind project to SonarCloud / SonarQube`, then click `Configure a new connection`
5. Click the `+` sign to add a new connection
6. Select `SonarCloud` and give the connection a name, e.g. Plant Simulator Connection
7. Click `Create Token` to log in to SonarCloud and automatically generate a connection token that will link SonarLint
   with the ControlOps organization
8. Click OK until you return to the original SonarLint configuration settings GUI. Select the new connection you made
   from the Connection drop down
9. Click `Search in list` next to the `Project key` field and select `control-ops_plant-simulator`, or manually enter
   "control-ops_plant-simulator" into the field
10. Click OK to create the sonarcloud connection

SonarLint should now automatically analyze your code as you write and flag any issues. Be sure to read its output and
fix issues as they come up; the CI/CD pipeline will prevent PRs containing issues from being merged. 