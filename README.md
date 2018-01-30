[![Build Status](https://travis-ci.org/red6/dmn-check.svg?branch=master)](https://travis-ci.org/red6/dmn-check)
[![Coverage Status](https://coveralls.io/repos/github/red6/dmn-check/badge.svg)](https://coveralls.io/github/red6/dmn-check)
[![Dependency Status](https://www.versioneye.com/user/projects/5a2183530fb24f0a6b514d78/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/5a2183530fb24f0a6b514d78)
[![Known Vulnerabilities](https://snyk.io/test/github/red6/dmn-check/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/red6/dmn-check?targetFile=pom.xml)
[![Maintainability](https://api.codeclimate.com/v1/badges/de1a1aa377520c44c3a7/maintainability)](https://codeclimate.com/github/red6/dmn-check/maintainability)

# DMN-Check Maven plugin

This is a Maven plugin which performs various static analyses on [Decision Model Notation (DMN)](https://en.wikipedia.org/wiki/Decision_Model_and_Notation) files to detect inconsistencies and bugs.

Currently the plugin checks among others for the following:
* Duplicate rules
* Conflicting rules
* Shadowed rules
* Types of the expressions

Below you find a complete list with detailed descriptions of what they do.

## Prerequisites

This plugin requires Java 8 or later and Apache Maven 3 or later. Some analyses are tailored towards the Camunda DMN implementation and might not work for different DMN implementations.

## Configuration

The following example shows the basic configuration of the plugin:
		
	        <plugin>
                <groupId>de.red6-es</groupId>
                <artifactId>dmn-check</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <phase>test</phase>
                        <goals>
                            <goal>check-dmn</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

Using this configuration the plugin will search all folders of the current project for files with the extension `.dmn`. It is possible to provide a set of search paths instead as well as to ignore certain files. The following example shows how you can make use of these options by restricting the search path to the folders `src/` and `model/`, as well as ignoring file `test.dmn`.

                <configuration>
                    <searchPaths>
                        <searchPath>src/</searchPath>
                        <searchPath>model/</searchPath>
                    </searchPaths>
                    <excludes>
                        <exclude>test.dmn</exclude>
                    </excludes>
                </configuration>
                
## Validations

The following subsections describe the available validations in detail. The DMN decision tables used in this section are derived from an example on [camunda.org](https://camunda.org/). Inputs are marked with `(I)` and outputs with `(O)` in the table headers.

### Duplicate rules

Consider the following DMN decision table with hit policy `UNIQUE`:

| Season (I)    | How many guests (I) | Dish (O)    |
| ------------- | ------------------- | ----------- |
| "Fall"        | <= 8                | "Spareribs" |
| "Winter"      | <= 8                | "Roastbeef" |
| "Spring"      | [5..8]              | "Steak"     |
| "Winter"      | <= 8                | "Roastbeef" |

It is pretty obvious that rule number two is a duplicate of rule number four and vice versa. This is not allowed by the `UNIQUE` hit policy and thus an error.

**Definition**:We say a rule is a duplicate of an other rule if and only if all inputs and outputs of those rules are identical.

`dmn-check` will report duplicate rules for all decision tables except for those with hit policy `COLLECT`. 
 
### Conflicting rules

Conflicting rules are somewhat similar to duplicate rules. Consider the following example with hit policy `UNIQUE`:

| Season (I)    | How many guests (I) | Dish (O)    |
| ------------- | ------------------- | ----------- |
| "Fall"        | <= 8                | "Spareribs" |
| "Winter"      | <= 8                | "Roastbeef" |
| "Spring"      | [5..8]              | "Steak"     |
| "Winter"      | <= 8                | "Stew"      |

We look again a rule two and four. This time all their inputs are identical, but they differ in the output. This is arguably worse than a duplicate rule since it may produce different results depending on the evaluation order of the decision table. Assuming that the runtime does not detect those inconsistencies.

**Definition**: We say rule `r` is in conflict with rule `s` if and only if all inputs of rules `r` and `s` are identical and if they differ in at lease one output. 

`dmn-check` will report duplicate rules for all decision tables except for those with hit policy `COLLECT` and `RULE_ORDER`. 

### Shadowed rules

Shadowing can also lead to strange misbehaviours that can be easy to stop but sometimes also very subtle. Have a look at the following example with hit policy `FIRST`:

| Season (I)    | How many guests (I) | Dish (O)    |
| ------------- | ------------------- | ----------- |
| "Fall"        | <= 8                | "Spareribs" |
| "Winter"      | <= 8                | "Roastbeef" |
| -             | -                   | "Stew"      |
| "Spring"      | [5..8]              | "Steak"     |

This example contains no duplicate rules and no conflicting rules. However all inputs of rule three are empty (represented with a dash in this example). As empty inputs match everything and since we assume hit policy `FIRST` rule four will never match as rule three matches for all possible inputs. Therefore stew is served to guests of 5 to 8 in Spring. Assuming that each rule serves a purpose shadowed rules are always a bug as they will never be matched.

**Definition**: Rule `r` shadows rule `s` if and only if the inputs of rule `r` matches at least for all values for which the inputs of rule `s` match.

`dmn-check` will report duplicate rules for all decision tables except for those with hit policy `COLLECT` and `RULE_ORDER`. 

### Types of expressions

DMN offers a rich expression language called FEEL that can be used to describe the conditions for the input entries. However, as with most expression languages, not all syntactically possible expressions are valid. `dmn-check` integrates a type checker for the FEEL expression language that ensures that a decision table contains only well-typed expressions. An example of an ill-typed expression is `[1..true]` which would describe the range between `1` and `true` which is (at lease in FEEL) not a valid expression. In contrast `[1..9]` is well-typed and describes the numbers from 1 to 9.