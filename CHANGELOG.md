## 1.3.0 (2023-02-04)

Features:

 - Make EnumValidator available in standalone mode (#86)
 - Provide a Docker image (ghcr.io/red6/dmn-check:latest)
 - Provide a picoli based cli

Misc:

 - A lot of dependency updates
 - Changes in the CI infrastructure

## 1.2.4 (2022-04-21)

Features:

 - Allow to specify the classpath for enum resolution explicitly.

## 1.2.3 (2022-04-18)

Bugfixes:

 - Only attempt to verify FEEL expressions and warn that other expression languages are currently unsupported (#83, #88, #98), thank you, @nairagit.

## 1.2.2 (2022-03-30)

Bugfixes:

 - Allow negative numbers in the typechecker (#87), thank you, @kishorehs123.


## 1.2.1 (2021-10-01)

Bugfixes:

  - Fix validation errors in the Gradle plugin.

## 1.2.0 (2021-09-03)

Features:

  - Validation Server
    With this release dmn-check includes a validation server module. This server is supposed to run standalone and
    accepts validation requests via HTTP. It is currently used for an experimental integration into the Camunda
    Modeler. A demo version is live at https://dmn-check.pascal-wittmann.de/demo/.
  - A Gradle plugin that allows you to use dmn-check in your Gradle projects.
  - A maven module plugin-base that provides common functionality for build system plugins.
  - Adds failOnWarning flag to support failing validation on Warning severity (#18), thank you, Krzysztof Barczynski.

Bugfixes:

  - Warn about conflicting rules for hit-policy collect and rule-order instead of reporting an error
  - Warn about duplicate rules for hit-policy collect instead of reporting an error

## 1.1.6 (2021-03-06)

Features:

  - Support DMN-1.3

Bugfixes:

  - Support for ItemComponents in ItemDefinitionAllowedValuesTypeValidator
    An ItemDefinition can consist of multiple ItemComponent with their own
    AllowedValues. Now the AllowedValues in ItemComponents are typechecked 
    as well.

## 1.1.5 (2020-11-27)

Bugfixes:

  - Fix subsumption for string literals (issue #9)
  - Enforce that negations are not nested in FEEL expressions
  - Refine subsumption for variables

## 1.1.4 (2020-06-06)

Bugfixes:

  - Support null as an expression (issue #7)
  - Ensure that a dash is not parsed as Empty within expressions

## 1.1.3 (2020-05-31)

Bugfixes:

  - Support expressions containing not in the subsumption check (issue #5)

Features:

  - Add support for range expression - literal subsumptions

## 1.1.2 (2020-03-09)

Bugfixes:

  - Assemble a JAR with all the dependencies for the server module

## 1.1.1 (2020-03-06)

Bugfixes:

  - Release missing module dmn-check-server
  - Change server port to 42000

## 1.1.0 (2020-03-04)

With release 1.1.0 dmn-check was split into four maven projects

  - core: containing api-level code
  - validators: containing all validators that ship with dmn-check
  - maven-plugin: containing the code for the Maven plugin
  - server: containing a Spark webserver providing a validation service
  
together with a Camunda modeler plugin that interacts with the validation server.

Bugfixes:

  - NullPointerException in enum validation
  
Features:

  - Validate that there is exactly one leaf in a requirement graph
  - Validate types of predefined input and output values
  - Validate types of allowed values in item definitions
  - Validate input data elements
  - Validate id and name of definitions elements

## 1.0.6 (2018-12-01)

Bugfixes:

  - not() can contain arbitrary well-typed expressions
  - Input variables without types are reported as warnings instead of errors

Features:

  - Validate knowledge sources
  - Validate requirement graphs (they have to be connected and acyclic) 
  - Validate that there is at least one decision
  - Separate validator classed and packages in the configuration

## 1.0.5 (2018-06-21)

Bugfixes:

  - Fix incompatibilities between maven 3.3 and 3.5

## 1.0.4 (2018-06-19)

Bugfixes:

  - Enum validator searches for classes on the project classpath
  
Features:

  - Check that id and name of decisions are set

## 1.0.3 (2018-06-12)

Bugfixes:

  - Fix detection of shadowed rules with strings

## 1.0.2 (2018-05-31)

Bugfixes:

  - Allow empty values in output entries

Features:

  - New Validator for Enum constants
  - Warn if an aggregation is used without a type
  - Sort validation results descending by severity
  - Improved error messages

## 1.0.1 (2018-04-19)

Bugfixes:

  - Allow empty strings in FEEL expressions
  
Features:

  - Implement subtyping for FEEL expressions (integer is subtype of long and double)

## 1.0.0 (2018-04-18)

Initial release.
