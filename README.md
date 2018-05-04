[![Build Status](https://travis-ci.org/red6/dmn-check.svg?branch=master)](https://travis-ci.org/red6/dmn-check)
[![Coverage Status](https://coveralls.io/repos/github/red6/dmn-check/badge.svg)](https://coveralls.io/github/red6/dmn-check)
[![Known Vulnerabilities](https://snyk.io/test/github/red6/dmn-check/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/red6/dmn-check?targetFile=pom.xml)
[![Maintainability](https://api.codeclimate.com/v1/badges/de1a1aa377520c44c3a7/maintainability)](https://codeclimate.com/github/red6/dmn-check/maintainability)
 [![Maven Central Version](https://img.shields.io/maven-central/v/de.redsix/dmn-check.svg)](http://search.maven.org/#search|gav|1|g:"de.redsix"%20AND%20a:"dmn-check")

# DMN-Check Maven plugin

This is a Maven plugin which performs various static analyses on [Decision Model Notation (DMN)](https://en.wikipedia.org/wiki/Decision_Model_and_Notation) files to detect inconsistencies and bugs.

Currently the plugin checks among others for the following:
* Duplicate rules
* Conflicting rules
* Shadowed rules
* Types of the expressions

In section [Validations](#validations) you find a complete list with detailed descriptions of what they do.

## Prerequisites

This plugin requires Java 8 or later and Apache Maven 3 or later. Some analyses are tailored towards the Camunda DMN implementation and might not work for different DMN implementations.

## Usage

`dmn-check` can be used either as a normal plugin inside your projects `pom.xml` or as a standalone program.

### Configuration in POM

The following example shows the basic configuration of the plugin:
		
	        <plugin>
                <groupId>de.redsix</groupId>
                <artifactId>dmn-check</artifactId>
                <version>...</version>
                <executions>
                    <execution>
                        <phase>test</phase>
                        <goals>
                            <goal>check-dmn</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

Using this configuration the plugin will search all folders of the current project for files with the extension `.dmn` and apply all available validators. It is possible to provide a set of search paths instead, as well as to ignore certain files and specify the validators that should be executed. The following example shows how you can make use of these options by restricting the search path to the folders `src/` and `model/`, as well as ignoring file `test.dmn`. The configuration further specifies that only the ShadowedRuleValidator should be executed. To specify validators you have to use the fully-qualified name.

                <configuration>
                    <searchPaths>
                        <searchPath>src/</searchPath>
                        <searchPath>model/</searchPath>
                    </searchPaths>
                    <excludes>
                        <exclude>test.dmn</exclude>
                    </excludes>
                    <validators>
                        <validator>de.redsix.dmncheck.validators.ShadowedRuleValidator</validator>
                    </validators>
                </configuration>
                
### Standalone usage

To use `dmn-check` without or outside of a Maven project you can invoke it in the following way

        mvn de.redsix:dmn-check:check-dmn

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

DMN offers a rich expression language called FEEL that can be used to describe the conditions for the input entries. However, as with most expression languages, not all syntactically possible expressions are valid. `dmn-check` integrates a severity checker for the FEEL expression language that ensures that a decision table contains only well-typed expressions. An example of an ill-typed expression is `[1..true]` which would describe the range between `1` and `true` which is (at lease in FEEL) not a valid expression. In contrast `[1..9]` is well-typed and describes the numbers from 1 to 9.


## Releated work

Althought there are not many tools for analysis of DMN files there exists some related work. Yet we were not aware of most of the releated work when starting the work on `dmn-check`.

### A Tool for the Analysis of DMN Decision Tables

Ülari Laurson and Fabrizio Maria Maggi extended the `dmn-js` editing toolkit of Camunda with analysis capabilities and published it at [github.com/ulaurson/dmn-js](https://github.com/ulaurson/dmn-js). The tool is able to detect syntax and type errors and to identify overlapping and missing rules. It also is able to simplify decision tables by merging rules. In the demo paper [LM16](#LM16) they describe the tool. Further details about the analyses performed by the tool are published in [CDL+16](#CDL+16). 

## References

<b id="CDL+16">CDL+16</b> Calvanese, D., Dumas, M., Laurson, Ü., Maggi, F.M., Montali, M., Teinemaa, I.: Semantics and analysis of DMN decision tables. In Proceedings of the 14th International Conference on Business Process Management (BPM) 2016

<b id="LM16">LM16</b> Laurson, Ü. and Maggi, F.M., 2016, September. A Tool for the Analysis of DMN Decision Tables. In BPM (Demos) (pp. 56-60).

<b id="BW-a">BW-a</b> Batoulis, K. and Weske, M., A Tool for Checking Soundness of Decision-Aware Business Processes.

<b id="BW-b">BW-b</b> Batoulis, K. and Weske, M., Disambiguation of DMN Decision Tables.

<b id="FMTV18">FMTV18</b> Figl, K., Mendling, J., Tokdemir, G. and Vanthienen, J., 2018. What we know and what we do not know about DMN. Enterprise Modelling and Information Systems Architectures, 13, pp.2-1.

<b id="Silver16">Silver16</b> Silver, B., 2016. Decision Table Analysis in DMN.

<b id="HDSV17">HDSV17</b> Hasic, F., De Smedt, J. and Vanthienen, J., 2017. Towards assessing the theoretical complexity of the decision model and notation (dmn). Enterprise, Business-Process and Information Systems Modeling. Springer International Publishing.
