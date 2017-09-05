DMN-Check maven plugin
======================

Checks for ambuguous dmn rules in current project


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
