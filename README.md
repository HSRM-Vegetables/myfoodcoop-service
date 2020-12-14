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

in order to build the project and start it. Alternatively, you can run `mvn package` to generate build the service and manually run it with java
API F

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