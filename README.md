[![Build Status](https://travis-ci.org/red6/dmn-check.svg?branch=master)](https://travis-ci.org/red6/dmn-check)
[![Coverage Status](https://coveralls.io/repos/github/red6/dmn-check/badge.svg)](https://coveralls.io/github/red6/dmn-check)
[![Dependency Status](https://www.versioneye.com/user/projects/5a2183530fb24f0a6b514d78/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/5a2183530fb24f0a6b514d78)

DMN-Check maven plugin
======================

Checks for ambiguous rules in [Decision Model Notation (DMN)](https://en.wikipedia.org/wiki/Decision_Model_and_Notation) files in the current project.


Configure:
		
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


Sample output:

    Exception in thread "main" java.lang.AssertionError: [Rule is defined more than once ["DE", "FEMALE", "OTHER", ] in File:/Users/developer/projects/dmn-project/src/main/resources/Example.dmn,


More to come...
