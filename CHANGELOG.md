## 1.1.4

Bugfixes:

  - Support null as an expression (issue #7)
  - Ensure that a dash is not parsed as Empty within expressions

## 1.1.3

Bugfixes:

  - Support expressions containing not in the subsumption check (issue #5)

Features:

  - Add support for range expression - literal subsumptions

## 1.1.2

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
