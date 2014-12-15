Product: Integration tests for WSO2 ESB Teamwork connector
Pre-requisites:

- Maven 3.x
- Java 1.6 or above

Tested Platform: 

- UBUNTU 14.04
- WSO2 ESB wso2esb-4.9.0-SNAPSHOT
- Java 1.7

STEPS:

1. Make sure the ESB 4.9.0-SNAPSHOT zip file at "/repository/".

2. Add following code block, just after the listeners block (Remove or comment all the other test blocks) in following file - "src/test/resources/testng.xml"

	<test name="Teamwork-Connector-Test" preserve-order="true" verbose="2">
        <packages>
            <package name="org.wso2.carbon.connector.integration.test.teamwork"/>
        </packages>
    </test> 

3. Copy proxy files to following location "src/test/resources/artifacts/ESB/config/proxies/teamwork/"

4. Copy request files to following "src/test/resources/artifacts/ESB/config/restRequests/teamwork/"

5. Edit the "teamwork.properties" at src/test/resources/artifacts/connector/config/ using valid and relevant data. Parameters to be changed are mentioned below.

	- proxyDirectoryRelativePath: relative path of the Rest Request files folder from target.
	- requestDirectoryRelativePath: relative path of proxy folder from target.
	- propertiesFilePath: relative path of properties file from target.
	- apiUrl: API URL.
	- clientId:The client ID.

		
6. Following data set can be used for the first testsuite run.

		proxyDirectoryRelativePath=/../src/test/resources/artifacts/ESB/config/proxies/teamwork/
		requestDirectoryRelativePath=/../src/test/resources/artifacts/ESB/config/restRequests/teamwork/
		propertiesFilePath=/../src/test/resources/artifacts/ESB/connector/config/
		apiUrl=https://kesan.teamwork.com
 		clientId=cod323ipad

7. Navigate to "org.wso2.carbon.connector/" and run the following command.
     $ mvn clean install
