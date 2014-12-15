/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.connector.integration.test.teamwork;

import org.apache.axis2.context.ConfigurationContext;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.automation.utils.axis2client.ConfigurationContextProvider;
import org.wso2.carbon.connector.integration.test.common.ConnectorIntegrationUtil;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.mediation.library.stub.MediationLibraryAdminServiceStub;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;

import javax.activation.DataHandler;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TeamworkConnectorIntegrationTest extends ESBIntegrationTest {
    private static final String CONNECTOR_NAME = "teamwork";

    private MediationLibraryUploaderStub mediationLibUploadStub = null;

    private MediationLibraryAdminServiceStub adminServiceStub = null;

    private ProxyServiceAdminClient proxyAdmin;

    private String repoLocation = null;

    private String teamworkConnectorFileName = CONNECTOR_NAME + ".zip";

    private Properties teamworkConnectorProperties = null;

    private String pathToProxiesDirectory = null;

    private String pathToRequestsDirectory = null;

    private Map<String, String> headersMap = new HashMap<String, String>();

    private String multipartProxyUrl;


    private String mId = null;
    private String mileId = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();

        ConfigurationContextProvider configurationContextProvider = ConfigurationContextProvider.getInstance();
        ConfigurationContext cc = configurationContextProvider.getConfigurationContext();
        mediationLibUploadStub =
                new MediationLibraryUploaderStub(cc, esbServer.getBackEndUrl() + "MediationLibraryUploader");
        AuthenticateStub.authenticateStub("admin", "admin", mediationLibUploadStub);

        adminServiceStub =
                new MediationLibraryAdminServiceStub(cc, esbServer.getBackEndUrl() + "MediationLibraryAdminService");

        AuthenticateStub.authenticateStub("admin", "admin", adminServiceStub);

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            repoLocation = System.getProperty("connector_repo").replace("/", "\\");
        } else {
            repoLocation = System.getProperty("connector_repo").replace("/", "/");
        }
        proxyAdmin = new ProxyServiceAdminClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());

        ConnectorIntegrationUtil.uploadConnector(repoLocation, mediationLibUploadStub, teamworkConnectorFileName);
        log.info("Sleeping for " + 30000 / 1000 + " seconds while waiting for synapse import");
        Thread.sleep(30000);

        adminServiceStub.updateStatus("{org.wso2.carbon.connector}" + CONNECTOR_NAME, CONNECTOR_NAME,
                "org.wso2.carbon.connector", "enabled");

        teamworkConnectorProperties = ConnectorIntegrationUtil.getConnectorConfigProperties(CONNECTOR_NAME);

        pathToProxiesDirectory = repoLocation + teamworkConnectorProperties.getProperty("proxyDirectoryRelativePath");
        pathToRequestsDirectory = repoLocation + teamworkConnectorProperties.getProperty("requestDirectoryRelativePath");

    }

    @Override
    protected void cleanup() {
        axis2Client.destroy();
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAccountDetails} integration test with mandatory parameter.")
    public void testGetAccountDetails() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAccountDetails.txt";
        String methodName = "tw_getAccountDetails";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("account"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAuthenticateDetails} integration test with mandatory parameter.")
    public void testGetAuthenticateDetails() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAuthenticateDetails.txt";
        String methodName = "tw_getAuthenticateDetails";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("account"));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getLatestActivity} integration test with mandatory parameter.")
    public void testGetLatestActivityWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getLatestActivityMandatory.txt";
        String methodName = "tw_getLatestActivity";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("activity"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getLatestActivity} integration test with optional parameter.")
    public void testGetLatestActivityWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getLatestActivityOptional.txt";
        String methodName = "tw_getLatestActivity";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("maxItems"), teamworkConnectorProperties.getProperty("onlyStarred"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("activity"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getLatestActivityForAProject} integration test with mandatory parameter.")
    public void testGetLatestActivityForAProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getLatestActivityForAProjectMandatory.txt";
        String methodName = "tw_getLatestActivityForAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("activity"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getLatestActivityForAProject} integration test with optional parameter.")
    public void testGetLatestActivityForAProjectWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getLatestActivityForAProjectOptional.txt";
        String methodName = "tw_getLatestActivityForAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"), teamworkConnectorProperties.getProperty("maxItems"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("activity"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getLatestActivityForAProject} integration test for negative case.")
    public void testGetLatestActivityForAProjectNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getLatestActivityForAProjectMandatory.txt";
        String methodName = "tw_getLatestActivityForAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /*
    @Test(groups = { "wso2.esb" }, description = "teamwork {deleteActivity} integration test with mandatory parameter.")
    public void testDeleteActivity() throws Exception {

        // Invoking the testGetCode method to derive the code which will be used to get access token
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteActivity.txt";
        String methodName = "tw_deleteActivity";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("activityId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"),"OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
*/

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAllCompanies} integration test with mandatory parameter.")
    public void testGetAllCompaniesWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAllCompaniesMandatory.txt";
        String methodName = "tw_getAllCompanies";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("companies"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getCompany} integration test with mandatory parameter.")
    public void testGetCompanyWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getCompanyMandatory.txt";
        String methodName = "tw_getCompany";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("companyId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("company"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getCompany} integration test for Negative case.")
    public void testGetCompanyNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getCompanyMandatory.txt";
        String methodName = "tw_getCompany";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidCompanyId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getCompaniesWithinAProject} integration test with mandatory parameter.")
    public void testGetCompaniesWithinAProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getCompaniesWithinAProjectMandatory.txt";
        String methodName = "tw_getCompaniesWithinAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("companies"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getCompaniesWithinAProject} integration test for negative case.")
    public void testGetCompaniesWithinAProjectNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getCompaniesWithinAProjectMandatory.txt";
        String methodName = "tw_getCompaniesWithinAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("companies"), "[]");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
/*
    @Test(groups = {"wso2.esb"}, description = "teamwork {createCompany} integration test with mandatory parameter.")
    public void testCreateCompanyWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createCompanyMandatory.txt";
        String methodName = "tw_createCompany";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,
                teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("companyName"),
                teamworkConnectorProperties.getProperty("addressOne"), teamworkConnectorProperties.getProperty("addressTwo"), teamworkConnectorProperties.getProperty("zip"),
                teamworkConnectorProperties.getProperty("city"), teamworkConnectorProperties.getProperty("state"), teamworkConnectorProperties.getProperty("countryCode"),
                teamworkConnectorProperties.getProperty("phone"), teamworkConnectorProperties.getProperty("fax"), teamworkConnectorProperties.getProperty("website"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {updateCompany} integration test with mandatory parameter.")
    public void testUpdateCompanyWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "updateCompanyMandatory.txt";
        String methodName = "tw_updateCompany";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,
                teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("updateCompanyId"), teamworkConnectorProperties.getProperty("updateCompanyName"),
                teamworkConnectorProperties.getProperty("addressOne"), teamworkConnectorProperties.getProperty("addressTwo"), teamworkConnectorProperties.getProperty("zip"),
                teamworkConnectorProperties.getProperty("city"), teamworkConnectorProperties.getProperty("state"), teamworkConnectorProperties.getProperty("countryCode"),
                teamworkConnectorProperties.getProperty("phone"), teamworkConnectorProperties.getProperty("fax"), teamworkConnectorProperties.getProperty("website"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }


    @Test(groups = {"wso2.esb"}, description = "teamwork {deleteCompany} integration test with mandatory parameter.")
    public void testDeleteCompanyWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteCompanyMandatory.txt";
        String methodName = "tw_deleteCompany";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("deleteCompanyId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {

            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
*/

    @Test(groups = {"wso2.esb"}, description = "teamwork {getFile} integration test with mandatory parameter.")
    public void testGetFileWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getFileMandatory.txt";
        String methodName = "tw_getFile";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("fileId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("file"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getFile} integration test for negative case.")
    public void testGetFileNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getFileMandatory.txt";
        String methodName = "tw_getFile";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidFileId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getJSONObject("file").getString("project-id"), "");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getFilesOnAProject} integration test with mandatory parameter.")
    public void testGetFilesOnAProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getFilesOnAProjectMandatory.txt";
        String methodName = "tw_getFilesOnAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("project"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getFilesOnAProject} integration test for negative case.")
    public void testGetFilesOnAProjectNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getFilesOnAProjectMandatory.txt";
        String methodName = "tw_getFilesOnAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /*    @Test(groups = { "wso2.esb" }, description = "teamwork {deleteFileFromProject} integration test with mandatory parameter.")
      public void testDeleteFileFromProjectWithMandatoryParameters() throws Exception {
          String jsonRequestFilePath = pathToRequestsDirectory + "deleteFileFromProjectMandatory.txt";
          String methodName = "tw_deleteFileFromProject";
          final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
          final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
          String modifiedJsonString = String.format(jsonString,teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("deleteFileId"));
          proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
          try {
              JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
              Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
          } finally {
              proxyAdmin.deleteProxy(methodName);
          }
      }
  */
/*
    @Test(groups = {"wso2.esb"}, description = "teamwork {uploadFile} integration test with mandatory parameters.")
    public void testUploadFileWithMandatoryParameters() throws Exception {
        String multipartPoxyName = teamworkConnectorProperties.getProperty("multipartProxyName");
        multipartProxyUrl = getProxyServiceURL(multipartPoxyName);
        ConnectorIntegrationUtil c = new ConnectorIntegrationUtil();
        ConnectorIntegrationUtil.MultipartFormdataProcessor multipartProcessor =
                c.new MultipartFormdataProcessor(multipartProxyUrl, headersMap);
        multipartProcessor.addFileToRequest("file", teamworkConnectorProperties.getProperty("uploadFileName"), null, teamworkConnectorProperties.getProperty("targetFileName"));
        RestResponse<JSONObject> esbRestResponse = multipartProcessor.processForJsonResponse();
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("invalidProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
*/
/*
    @Test(groups = {"wso2.esb"}, description = "teamwork {addFileToProject} integration test with mandatory parameter.")
    public void testAddFileToProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "addFileToProjectMandatory.txt";
        String methodName = "tw_addFileToProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("fileProjectId"),
                teamworkConnectorProperties.getProperty("fileName"), teamworkConnectorProperties.getProperty("fileDescription"), teamworkConnectorProperties.getProperty("filePrivate"), teamworkConnectorProperties.getProperty("fileCategoryId"),
                teamworkConnectorProperties.getProperty("fileCategoryName"), teamworkConnectorProperties.getProperty("addFilePendingFileRef"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("fileId"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {addNewVersionToFile} integration test with mandatory parameter.")
    public void testAddNewVersionToFileWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "addNewVersionToFileMandatory.txt";
        String methodName = "tw_addNewVersionToFile";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("newVersionFileId"),
                teamworkConnectorProperties.getProperty("newVersionPendingFileRef"), teamworkConnectorProperties.getProperty("fileDescription"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("versionNo"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
*/
    @Test(groups = {"wso2.esb"}, description = "teamwork {getPerson} integration test with mandatory parameter.")
    public void testGetPersonWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getPersonMandatory.txt";
        String methodName = "tw_getPerson";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("personId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("person"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getPerson} integration test with mandatory parameter.")
    public void testGetPersonNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getPersonMandatory.txt";
        String methodName = "tw_getPerson";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidPersonId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getPeopleInProject} integration test with mandatory parameter.")
    public void testGetPeopleInProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getPeopleInProjectMandatory.txt";
        String methodName = "tw_getPeopleInProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("people"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getPeopleInProject} integration test for negative case.")
    public void testGetPeopleInProjectNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getPeopleInProjectMandatory.txt";
        String methodName = "tw_getPeopleInProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("people"), "[]");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getPeopleInCompany} integration test with mandatory parameter.")
    public void testGetPeopleInCompanyWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getPeopleInCompanyMandatory.txt";
        String methodName = "tw_getPeopleInCompany";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("companyId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("people"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getPeopleInCompany} integration test for negative case.")
    public void testGetPeopleInCompanyNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getPeopleInCompanyMandatory.txt";
        String methodName = "tw_getPeopleInCompany";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidCompanyId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("people"), "[]");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getPeople} integration test with mandatory parameter.")
    public void testGetPeopleWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getPeopleMandatory.txt";
        String methodName = "tw_getPeople";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("people"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getPeople} integration test with optional parameter.")
    public void testGetPeopleWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getPeopleOptional.txt";
        String methodName = "tw_getPeople";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("page"), teamworkConnectorProperties.getProperty("pageSize"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("people"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getCurrentUser} integration test with optional parameter.")
    public void testGetCurrentUserWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getCurrentUserMandatory.txt";
        String methodName = "tw_getCurrentUser";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("person"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAPIKeyForPeopleOnAccount} integration test with optional parameter.")
    public void testGetAPIKeyForPeopleOnAccountWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAPIKeyForPeopleOnAccountMandatory.txt";
        String methodName = "tw_getAPIKeyForPeopleOnAccount";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("people"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /*
        @Test(groups = { "wso2.esb" },description = "teamwork {createUser} integration test with mandatory parameter.")
        public void testCreateUserWithMandatoryParameters() throws Exception {
            String jsonRequestFilePath = pathToRequestsDirectory + "createUserMandatory.txt";
            String methodName = "tw_createUser";
            final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
            final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
            String modifiedJsonString = String.format(jsonString,
                    teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("firstName"),
                    teamworkConnectorProperties.getProperty("lastName"),teamworkConnectorProperties.getProperty("emailAddress"),teamworkConnectorProperties.getProperty("userType"),
                    teamworkConnectorProperties.getProperty("userName"),teamworkConnectorProperties.getProperty("password"),teamworkConnectorProperties.getProperty("userCompanyId"),teamworkConnectorProperties.getProperty("title"),
                    teamworkConnectorProperties.getProperty("mobileNo"),teamworkConnectorProperties.getProperty("officePhoneNo"),teamworkConnectorProperties.getProperty("officePhoneNoExt"),
                    teamworkConnectorProperties.getProperty("faxNo"),teamworkConnectorProperties.getProperty("homePhoneNumber"),teamworkConnectorProperties.getProperty("imHandle"),
                    teamworkConnectorProperties.getProperty("imService"),teamworkConnectorProperties.getProperty("dateFormat"),teamworkConnectorProperties.getProperty("sendWelcomeEmail"),
                    teamworkConnectorProperties.getProperty("welcomeEmailMessage"),teamworkConnectorProperties.getProperty("receiveDailyReports"),teamworkConnectorProperties.getProperty("autoGiveProjectAccess"),
                    teamworkConnectorProperties.getProperty("openID"),teamworkConnectorProperties.getProperty("notes"),teamworkConnectorProperties.getProperty("userLanguage"),
                    teamworkConnectorProperties.getProperty("administrator"),teamworkConnectorProperties.getProperty("canAddProjects"),teamworkConnectorProperties.getProperty("timezoneId") );
            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            try {
                JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
                Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
            } finally {
                proxyAdmin.deleteProxy(methodName);
            }
        }

        @Test(groups = { "wso2.esb" },description = "teamwork {updateUser} integration test with mandatory parameter.")
        public void testUpdateUserWithMandatoryParameters() throws Exception {
            String jsonRequestFilePath = pathToRequestsDirectory + "updateUserMandatory.txt";
            String methodName = "tw_updateUser";
            final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
            final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
            String modifiedJsonString = String.format(jsonString,
                    teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("updatePersonId"),teamworkConnectorProperties.getProperty("updateFirstName"),
                    teamworkConnectorProperties.getProperty("lastName"),teamworkConnectorProperties.getProperty("updateEmail"),
                    teamworkConnectorProperties.getProperty("username"),teamworkConnectorProperties.getProperty("password"),teamworkConnectorProperties.getProperty("userCompanyId"),teamworkConnectorProperties.getProperty("title"),
                    teamworkConnectorProperties.getProperty("mobileNo"),teamworkConnectorProperties.getProperty("officePhoneNo"),teamworkConnectorProperties.getProperty("officePhoneNoExt"),
                    teamworkConnectorProperties.getProperty("faxNo"),teamworkConnectorProperties.getProperty("homePhoneNumber"),teamworkConnectorProperties.getProperty("imHandle"),
                    teamworkConnectorProperties.getProperty("imService"),teamworkConnectorProperties.getProperty("dateFormat"),teamworkConnectorProperties.getProperty("sendWelcomeEmail"),
                    teamworkConnectorProperties.getProperty("welcomeEmailMessage"),teamworkConnectorProperties.getProperty("receiveDailyReports"),teamworkConnectorProperties.getProperty("autoGiveProjectAccess"),
                    teamworkConnectorProperties.getProperty("openID"),teamworkConnectorProperties.getProperty("notes"),teamworkConnectorProperties.getProperty("userLanguage"),
                    teamworkConnectorProperties.getProperty("administrator"),teamworkConnectorProperties.getProperty("canAddProjects"),teamworkConnectorProperties.getProperty("timezoneId") );
            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            try {
                JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
                Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
            } finally {
                proxyAdmin.deleteProxy(methodName);
            }
        }

        @Test(groups = { "wso2.esb" }, description = "teamwork {deleteUser} integration test with mandatory parameter.")
        public void testDeleteUserWithMandatoryParameters() throws Exception {
            String jsonRequestFilePath = pathToRequestsDirectory + "deleteUserMandatory.txt";
            String methodName = "tw_deleteUser";
            final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
            final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
            String modifiedJsonString = String.format(jsonString,teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("deletePersonId"));
            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            try {
                JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
                Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
            } finally {
                proxyAdmin.deleteProxy(methodName);
            }
        }
    */
    @Test(groups = {"wso2.esb"}, description = "teamwork {getUserPermissionsOnProject} integration test with mandatory parameter.")
    public void testGetUserPermissionsOnProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getUserPermissionsOnProjectMandatory.txt";
        String methodName = "tw_getUserPermissionsOnProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"), teamworkConnectorProperties.getProperty("userId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("people"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getUserPermissionsOnProject} integration test for negative case.")
    public void testGetUserPermissionsNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getUserPermissionsOnProjectMandatory.txt";
        String methodName = "tw_getUserPermissionsOnProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"), teamworkConnectorProperties.getProperty("invalidUserId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("people"));
            Assert.assertEquals(jsonObject.getString("people"), "[]");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {addUserToProject} integration test with mandatory parameter.")
    public void testAddUserToProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "addUserToProjectMandatory.txt";
        String methodName = "tw_addUserToProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("permissionProjectId"),
                teamworkConnectorProperties.getProperty("permissionUserId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {updateUserPermissionOnProject} integration test with mandatory parameter.")
    public void testUpdateUserPermissionOnProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "updateUserPermissionOnProjectMandatory.txt";
        String methodName = "tw_updateUserPermissionOnProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,
                teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("updatePermissionProjectId"), teamworkConnectorProperties.getProperty("permissionUserId"),
                teamworkConnectorProperties.getProperty("viewMessagesAndFiles"), teamworkConnectorProperties.getProperty("viewTasksAndMilestones"),
                teamworkConnectorProperties.getProperty("viewTime"), teamworkConnectorProperties.getProperty("viewNotebooks"), teamworkConnectorProperties.getProperty("viewRiskRegister"), teamworkConnectorProperties.getProperty("viewInvoices"),
                teamworkConnectorProperties.getProperty("viewLinks"), teamworkConnectorProperties.getProperty("addTasks"), teamworkConnectorProperties.getProperty("addMilestones"),
                teamworkConnectorProperties.getProperty("addTaskLists"), teamworkConnectorProperties.getProperty("addMessages"), teamworkConnectorProperties.getProperty("addFiles"),
                teamworkConnectorProperties.getProperty("addTime"), teamworkConnectorProperties.getProperty("addNotebooks"), teamworkConnectorProperties.getProperty("addLinks"),
                teamworkConnectorProperties.getProperty("setPrivacy"), teamworkConnectorProperties.getProperty("canBeAssignedToTasksAndMilestones"), teamworkConnectorProperties.getProperty("projectAdministrator"),
                teamworkConnectorProperties.getProperty("addPeopleToProject"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }


    @Test(groups = {"wso2.esb"}, description = "teamwork {removeUserFromProject} integration test with mandatory parameter.")
    public void testRemoveUserFromProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "removeUserFromProjectMandatory.txt";
        String methodName = "tw_removeUserFromProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("permissionProjectId"),
                teamworkConnectorProperties.getProperty("permissionUserId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {listRolesOnProject} integration test with mandatory parameter.")
    public void testListRolesOnProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "listRolesOnProjectMandatory.txt";
        String methodName = "tw_listRolesOnProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("roles"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getMessage} integration test with mandatory parameter.")
    public void testGetMessageWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getMessageMandatory.txt";
        String methodName = "tw_getMessage";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("messageId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("post"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getMessage} integration test for negative case.")
    public void testGetMessageNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getMessageMandatory.txt";
        String methodName = "tw_getMessage";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidMessageId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getLatestMessages} integration test with mandatory parameter.")
    public void testGetLatestMessagesWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getLatestMessagesMandatory.txt";
        String methodName = "tw_getLatestMessages";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("posts"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {createMessage} integration test with mandatory parameter.")
    public void testCreateMessageWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createMessageMandatory.txt";
        String methodName = "tw_createMessage";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,
                teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("messageProjectId"),
                teamworkConnectorProperties.getProperty("messageTitle"), teamworkConnectorProperties.getProperty("messageCategoryId"), teamworkConnectorProperties.getProperty("notify"),
                teamworkConnectorProperties.getProperty("private"), teamworkConnectorProperties.getProperty("messageBody"), teamworkConnectorProperties.getProperty("attachments"), teamworkConnectorProperties.getProperty("pendingFileAttachments"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("messageId"));
            mId = jsonObject.getString("messageId");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }


    @Test(groups = {"wso2.esb"}, description = "teamwork {updateMessage} integration test with mandatory parameter.")
    public void testUpdateMessageWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "updateMessageMandatory.txt";
        String methodName = "tw_updateMessage";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,
                teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("updateMessageId"),
                teamworkConnectorProperties.getProperty("messageTitle"), teamworkConnectorProperties.getProperty("messageCategoryId"), teamworkConnectorProperties.getProperty("notify"),
                teamworkConnectorProperties.getProperty("private"), teamworkConnectorProperties.getProperty("messageBody"), teamworkConnectorProperties.getProperty("attachments"), teamworkConnectorProperties.getProperty("pendingFileAttachments"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 200);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {deleteMessage} integration test with mandatory parameter.")
    public void testDeleteMessageWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteMessageMandatory.txt";
        String methodName = "tw_deleteMessage";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), mId);
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 200);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getProject} integration test with mandatory parameter.")
    public void testGetProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getProjectMandatory.txt";
        String methodName = "tw_getProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("project"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getProject} integration test with optional parameter.")
    public void testGetProjectWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getProjectOptional.txt";
        String methodName = "tw_getProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"), teamworkConnectorProperties.getProperty("includePeople"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("project"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getProject} integration test for negative case.")
    public void testGetProjectNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getProjectMandatory.txt";
        String methodName = "tw_getProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getStarredProjects} integration test with mandatory parameter.")
    public void testGetStarredProjectsWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getStarredProjectsMandatory.txt";
        String methodName = "tw_getStarredProjects";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("projects"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAllProjects} integration test with mandatory parameter.")
    public void testGetAllProjectsWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAllProjectsMandatory.txt";
        String methodName = "tw_getAllProjects";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("projects"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAllProjects} integration test with optional parameter.")
    public void testGetAllProjectsWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAllProjectsOptional.txt";
        String methodName = "tw_getAllProjects";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"),
                teamworkConnectorProperties.getProperty("getProjectStatus"), teamworkConnectorProperties.getProperty("projectUpdatedAfterDate"), teamworkConnectorProperties.getProperty("projectUpdatedAfterTime"),
                teamworkConnectorProperties.getProperty("projectOrderby"), teamworkConnectorProperties.getProperty("projectCreatedAfterDate"), teamworkConnectorProperties.getProperty("projectCreatedAfterTime"),
                teamworkConnectorProperties.getProperty("projectIncludePeople"), teamworkConnectorProperties.getProperty("projectPage"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("projects"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /*
        @Test(groups = {"wso2.esb"}, description = "teamwork {createProject} integration test with mandatory parameter.")
        public void testCreateProjectWithMandatoryParameters() throws Exception {
            String jsonRequestFilePath = pathToRequestsDirectory + "createProjectMandatory.txt";
            String methodName = "tw_createProject";
            final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
            final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
            String modifiedJsonString = String.format(jsonString,
                    teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("createProjectName"),
                    teamworkConnectorProperties.getProperty("projectDescription"), teamworkConnectorProperties.getProperty("projectStartDate"), teamworkConnectorProperties.getProperty("projectEndDate"),
                    teamworkConnectorProperties.getProperty("projectCompanyId"), teamworkConnectorProperties.getProperty("projectNewCompany"), teamworkConnectorProperties.getProperty("projectCategoryId"));
            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            try {
                JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
                Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
            } finally {
                proxyAdmin.deleteProxy(methodName);
            }
        }

        @Test(groups = {"wso2.esb"}, description = "teamwork {updateProject} integration test with mandatory parameter.")
        public void testUpdateProjectWithMandatoryParameters() throws Exception {
            String jsonRequestFilePath = pathToRequestsDirectory + "updateProjectMandatory.txt";
            String methodName = "tw_updateProject";
            final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
            final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
            String modifiedJsonString = String.format(jsonString,
                    teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("updateProjectId"),
                    teamworkConnectorProperties.getProperty("projectName"), teamworkConnectorProperties.getProperty("projectDescription"), teamworkConnectorProperties.getProperty("projectStartDate"),
                    teamworkConnectorProperties.getProperty("projectEndDate"), teamworkConnectorProperties.getProperty("projectCompanyId"), teamworkConnectorProperties.getProperty("projectNewCompany"), teamworkConnectorProperties.getProperty("projectStatus"),
                    teamworkConnectorProperties.getProperty("projectCategoryId"));
            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            try {
                JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
                Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
            } finally {
                proxyAdmin.deleteProxy(methodName);
            }
        }
    */
/*  @Test(groups = { "wso2.esb" }, description = "teamwork {deleteProject} integration test with mandatory parameter.")
    public void testDeleteProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteProjectMandatory.txt";
        String methodName = "tw_deleteProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("deleteProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 200);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
*/
    @Test(groups = {"wso2.esb"}, description = "teamwork {starAProject} integration test with mandatory parameter.")
    public void testStarAProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "starAProjectMandatory.txt";
        String methodName = "tw_starAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("starProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 200);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {unstarAProject} integration test with mandatory parameter.")
    public void testUnstarAProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "unstarAProjectMandatory.txt";
        String methodName = "tw_unstarAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("unstarProjectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 200);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getEvent} integration test with mandatory parameter.")
    public void testGetEventWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getEventMandatory.txt";
        String methodName = "tw_getEvent";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("eventId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("event"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getEvent} integration test for negative case.")
    public void testGetEventNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getEventMandatory.txt";
        String methodName = "tw_getEvent";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidEventId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAllEventTypes} integration test with mandatory parameter.")
    public void testGetAllEventTypesWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAllEventTypesMandatory.txt";
        String methodName = "tw_getAllEventTypes";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("eventtypes"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAllEvents} integration test with mandatory parameter.")
    public void testGetAllEventsWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAllEventsMandatory.txt";
        String methodName = "tw_getAllEvents";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("startdate"), teamworkConnectorProperties.getProperty("endDate"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("events"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAllEvents} integration test with optional parameter.")
    public void testGetAllEventsWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAllEventsOptional.txt";
        String methodName = "tw_getAllEvents";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("startdate"), teamworkConnectorProperties.getProperty("endDate"), teamworkConnectorProperties.getProperty("showDeleted"), teamworkConnectorProperties.getProperty("updatedAfterDate"), teamworkConnectorProperties.getProperty("page"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("events"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    /*
        @Test(groups = { "wso2.esb" },description = "teamwork {createEvent} integration test with mandatory parameter.")
        public void testCreateEventWithMandatoryParameters() throws Exception {
            String jsonRequestFilePath = pathToRequestsDirectory + "createEventMandatory.txt";
            String methodName = "tw_createEvent";
            final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
            final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
            String modifiedJsonString = String.format(jsonString,
                    teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("eventStart"),
                    teamworkConnectorProperties.getProperty("eventEnd"),teamworkConnectorProperties.getProperty("eventAllDay"),teamworkConnectorProperties.getProperty("eventTitle"),
                    teamworkConnectorProperties.getProperty("eventDescription"),teamworkConnectorProperties.getProperty("eventWhere"),teamworkConnectorProperties.getProperty("eventType"),teamworkConnectorProperties.getProperty("eventProjectId"),
                    teamworkConnectorProperties.getProperty("eventShowAsBusy"),teamworkConnectorProperties.getProperty("createEId"),teamworkConnectorProperties.getProperty("eventAttendingUserIds"),
                    teamworkConnectorProperties.getProperty("eventNotifyUserIds"),teamworkConnectorProperties.getProperty("eventAttendeesCanEdit"),teamworkConnectorProperties.getProperty("eventProjectUsersCanEdit"),
                    teamworkConnectorProperties.getProperty("eventReminderType"),teamworkConnectorProperties.getProperty("eventPeriod"),teamworkConnectorProperties.getProperty("eventBefore"));
            proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
            try {
                JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
                Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
            } finally {
                proxyAdmin.deleteProxy(methodName);
            }
        }
        */
/*
    @Test(groups = { "wso2.esb" },description = "teamwork {updateEvent} integration test with mandatory parameter.")
    public void testUpdateEventWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "updateEventMandatory.txt";
        String methodName = "tw_updateEvent";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,
                teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("updateEventId"),teamworkConnectorProperties.getProperty("eventStart"),
                teamworkConnectorProperties.getProperty("eventEnd"),teamworkConnectorProperties.getProperty("eventAllDay"),teamworkConnectorProperties.getProperty("eventTitle"),
                teamworkConnectorProperties.getProperty("eventDescription"),teamworkConnectorProperties.getProperty("eventWhere"),teamworkConnectorProperties.getProperty("eventType"),teamworkConnectorProperties.getProperty("eventProjectId"),
                teamworkConnectorProperties.getProperty("eventShowAsBusy"),teamworkConnectorProperties.getProperty("createEId"),teamworkConnectorProperties.getProperty("eventAttendingUserIds"),
                teamworkConnectorProperties.getProperty("eventNotifyUserIds"),teamworkConnectorProperties.getProperty("eventAttendeesCanEdit"),teamworkConnectorProperties.getProperty("eventProjectUsersCanEdit"),
                teamworkConnectorProperties.getProperty("eventReminderType"),teamworkConnectorProperties.getProperty("eventPeriod"),teamworkConnectorProperties.getProperty("eventBefore"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

*/
/*
    @Test(groups = { "wso2.esb" }, description = "teamwork {deleteEvent} integration test with mandatory parameter.")
    public void testDeleteEventWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteEventMandatory.txt";
        String methodName = "tw_deleteEvent";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,teamworkConnectorProperties.getProperty("apiUrl"),teamworkConnectorProperties.getProperty("clientId"),teamworkConnectorProperties.getProperty("deleteEventId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertEquals(jsonObject.getString("STATUS"), "OK");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
*/
    @Test(groups = {"wso2.esb"}, description = "teamwork {getMilestone} integration test with mandatory parameter.")
    public void testGetMilestoneWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getMilestoneMandatory.txt";
        String methodName = "tw_getMilestone";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("milestoneId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("milestone"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getMilestone} integration test with optional parameter.")
    public void testGetMilestoneWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getMilestoneOptional.txt";
        String methodName = "tw_getMilestone";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("milestoneId"),
                teamworkConnectorProperties.getProperty("getProgress"), teamworkConnectorProperties.getProperty("showTaskLists"), teamworkConnectorProperties.getProperty("showTasks"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("milestone"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getMilestone} integration test for negative case.")
    public void testGetMilestoneNegativeCase() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getMilestoneMandatory.txt";
        String methodName = "tw_getMilestone";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("invalidMilestoneId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 404);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAllMilestones} integration test with mandatory parameter.")
    public void testGetAllMilestonesWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAllMilestonesMandatory.txt";
        String methodName = "tw_getAllMilestones";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("milestones"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getAllMilestones} integration test with optional parameter.")
    public void testGetAllMilestonesWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getAllMilestonesOptional.txt";
        String methodName = "tw_getAllMilestones";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("find"), teamworkConnectorProperties.getProperty("getProgress"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("milestones"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getMilestonesOnAProject} integration test with mandatory parameter.")
    public void testGetMilestonesOnAProjectWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getMilestonesOnAProjectMandatory.txt";
        String methodName = "tw_getMilestonesOnAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("milestones"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {getMilestonesOnAProject} integration test with optional parameter.")
    public void testGetMilestonesOnAProjectWithOptionalParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "getMilestonesOnAProjectOptional.txt";
        String methodName = "tw_getMilestonesOnAProject";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("projectId"), teamworkConnectorProperties.getProperty("find"), teamworkConnectorProperties.getProperty("getProgress"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("milestones"));
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
/*
    @Test(groups = {"wso2.esb"}, description = "teamwork {createMilestone} integration test with mandatory parameter.")
    public void testCreateMilestoneWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "createMilestoneMandatory.txt";
        String methodName = "tw_createMilestone";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString,
                teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("milestoneProjectId"),
                teamworkConnectorProperties.getProperty("milestoneTitle"), teamworkConnectorProperties.getProperty("milestoneDescription"), teamworkConnectorProperties.getProperty("milestoneDeadline"),
                teamworkConnectorProperties.getProperty("milestoneNotify"), teamworkConnectorProperties.getProperty("milestoneReminder"), teamworkConnectorProperties.getProperty("milestoneResponsiblePartyIds"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("milestoneId"));
           mileId=jsonObject.getString("milestoneId");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {deleteMilestone} integration test with mandatory parameter.")
    public void testDeleteMilestoneWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "deleteMilestoneMandatory.txt";
        String methodName = "tw_deleteMilestone";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), mileId);
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 200);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }
*/
    @Test(groups = {"wso2.esb"}, description = "teamwork {completeAMilestone} integration test with mandatory parameter.")
    public void testCompleteAMilestoneWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "completeAMilestoneMandatory.txt";
        String methodName = "tw_completeAMilestone";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("completeMilestoneId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 200);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "teamwork {uncompleteAMilestone} integration test with mandatory parameter.")
    public void testUncompleteAMilestoneWithMandatoryParameters() throws Exception {
        String jsonRequestFilePath = pathToRequestsDirectory + "uncompleteAMilestoneMandatory.txt";
        String methodName = "tw_uncompleteAMilestone";
        final String jsonString = ConnectorIntegrationUtil.getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory + methodName + ".xml";
        String modifiedJsonString = String.format(jsonString, teamworkConnectorProperties.getProperty("apiUrl"), teamworkConnectorProperties.getProperty("clientId"), teamworkConnectorProperties.getProperty("uncompleteMilestoneId"));
        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        try {
            int responseHeader = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(responseHeader == 200);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

}