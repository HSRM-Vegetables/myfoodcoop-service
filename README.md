# Stadtgemüse Backend

## Installation

### Prerequisites

This project runs on Java version 15 and uses maven as a building and tooling framework. In order to start this project and develop for it, Java 15 and Maven needs to be installed.

 - Installing Maven https://maven.apache.org/install.html
 - Installing Java https://openjdk.java.net/install/
 
### Installing Dependencies
 
 Dependencies are managed via the pom.yml file. This file contains all direct dependencies of this project and some additional configuration. To install these dependencies, run 
 
 `mvn clean install`
 
 This will delete all prior build output, download all dependencies, and generate necessary classes. 
 
### Starting the Service

When all dependencies were downloaded and installed, run 

`mvn spring-boot:run`

in order to build the project and start it. Alternatively, you can run `mvn package` to generate build the service and manually run it with java.
See the following section to select the correct profile

### Maven Profiles
Maven comes with the ability to save configuration values in files, located in src/main/resources/application-{profile}.yml.
To maintain different configurations for different environments (e.g. production or local development), next to the default application.yml file,
additional files can be created that override or extend all default values. 

These profiles are available:

| Profile Name | File                    | Description                                                                                                |
|--------------|-------------------------|------------------------------------------------------------------------------------------------------------|
| default      | application.yml         | Contains all config values. **Secrets must not be stored in this file**                                    |
| local        | application-local.yml   | Contains overrides for local development **Secrets must not be stored in this file**                       |
| secrets      | application-secrets.yml | Contains secrets for **local** development. This file must be created yourself **Do not commit this file** |

For local development, both the local and the secrets profile must be active.

#### Activate a profile via the Spring Boot CLI
To select all necessary profiles for local development, use this CLI switch:
* -Dspring-boot.run.profiles=local,secrets

example: `mvn spring-boot:run -Dspring-boot.run.profiles=local,secrets`

#### Activate a profile via the Maven CLI
If you're starting the project not via the maven spring-boot extension, but via maven itself, add the following switch:
* -Dspring.profiles.active=local,secrets

#### Activate a profile via IntelliJ Ultimate
IntelliJ Ultimate comes with the Spring Boot extension which helps with running Spring Boot projects
To select profiles, go to Configuration Selection (top right) -> Edit Configurations. Select the spring config you're using to run the project, 
and add the switch under Configuration -> Environment -> VM Options:
* -Dspring.profiles.active=local,secrets

#### Activate a profile via IntelliJ Community
To select profiles, go to Configuration Selection (top right) -> Edit Configurations. Select the maven config you're using to run the project,
and add the switch under Parameters -> Command Line:
* spring-boot:run -Dspring-boot.run.fork=false -Dspring-boot.run.profiles=local,secrets

The `-Dspring-boot.run.fork=false` disables forking while running the project, which helps when attaching the debugger.

### Starting a local PostgreSQL Instance
This service uses a PostgreSQL database to persist data. For the convenience of the developer, a docker-compose.yaml was provided. 
In order to use this file, please refer to installing docker for your system, also make sure to install the docker-compose extension.
To start the database (and adminier, a tool to manage this database) simply run `docker-compose up` in the root directory of this project.
If not otherwise specified, the username is `postgres` and the password can be found in the docker-compose.yaml

## API Specification

This service is designed "API First", thus utilizing the OpenAPI specification found in documentation/openapi.yml to generate stub controllers and all models sent to and responded by this service.
In order to create a new route, alter the response model of a route or add a request parameter, simply change the specification and re-generate all classes by running `mvn clean install`.

For further information about the OpenAPI specification, please see https://swagger.io/specification/

## Testing

Tests are one of the most important parts when it comes to writing software, but often developers see it as a tedious chore rather than integral to their work.
To make writing tests easy, this service uses the Karate DSL and test runner. See https://github.com/intuit/karate for further information.

Tests are automatically run, when the project is being build (`mvn package`) or installed (`mvn clean install`). To skip running the tests, add `-DskipTests`. More information can be found at https://maven.apache.org/plugins-archives/maven-surefire-plugin-2.12.4/examples/skipping-test.html 

## Error Handling

All Errors thrown by either Spring itself or by the service are handled in src/main/java/de/hsrm/vegetables/service/exception/GlobalResponseEntityExceptionHandler.java and return a harmonized scheme.
Errors which are not specifically mapped in this file respond with a generic "Interal Server Error" message. 

All custom Errors must inherit the abstract BaseError class in src/main/java/de/hsrm/vegetables/service/exception/errors, if a specific object with a custom message shall be returned, create a @ErrorHandler in the GlobalResponseEntityExceptionHandler.
An example is provided via the ExampleError class. 

### Schema Validation Errors
Schema validation errors are automatically detected and handled. In order for that to work, the openapi.yml specification must be as detailed as possible concerning required field in objects, min/max values, string lengths, etc. 

## Getting Help

The HELP.md contains a few useful links, go have a look.
