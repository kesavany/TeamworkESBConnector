package org.wso2.carbon.connector.integration.test.common;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;
import org.wso2.carbon.mediation.library.stub.upload.types.carbon.LibraryFileItem;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.*;

public class ConnectorIntegrationUtil {
    public static final String ESB_CONFIG_LOCATION = "artifacts" + File.separator + "ESB" + File.separator + "config";

    private static final Log log = LogFactory.getLog(ConnectorIntegrationUtil.class);
    protected static final int MULTIPART_TYPE_RELATED = 100001;
    //protected  static String pathToResourcesDirectory;


    public static void uploadConnector(String repoLocation, MediationLibraryUploaderStub mediationLibUploadStub,
                                       String strFileName) throws MalformedURLException, RemoteException {

        List<LibraryFileItem> uploadLibraryInfoList = new ArrayList<LibraryFileItem>();
        LibraryFileItem uploadedFileItem = new LibraryFileItem();
        uploadedFileItem.setDataHandler(new DataHandler(new URL("file:" + "///" + repoLocation + "/" + strFileName)));
        uploadedFileItem.setFileName(strFileName);
        uploadedFileItem.setFileType("zip");
        uploadLibraryInfoList.add(uploadedFileItem);
        LibraryFileItem[] uploadServiceTypes = new LibraryFileItem[uploadLibraryInfoList.size()];
        uploadServiceTypes = uploadLibraryInfoList.toArray(uploadServiceTypes);
        mediationLibUploadStub.uploadLibrary(uploadServiceTypes);
    }

    public static int sendRequestToRetriveHeaders(String addUrl, String query) throws IOException, JSONException {

        String charset = "UTF-8";
        URLConnection connection = new URL(addUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);

        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(query.getBytes(charset));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                    log.error("Error while closing the connection");
                }
            }
        }

        HttpURLConnection httpConn = (HttpURLConnection) connection;
        int responseCode = httpConn.getResponseCode();

        return responseCode;
    }

    public static int sendRequestToRetriveHeaders(String addUrl, String query, String contentType) throws IOException,
            JSONException {

        String charset = "UTF-8";
        URLConnection connection = new URL(addUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", contentType + ";charset=" + charset);

        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(query.getBytes(charset));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                    log.error("Error while closing the connection");
                }
            }
        }

        HttpURLConnection httpConn = (HttpURLConnection) connection;
        int responseCode = httpConn.getResponseCode();

        return responseCode;
    }

    public static String sendRequest_String(String addUrl, String query) throws IOException, JSONException {

        String charset = "UTF-8";
        URLConnection connection = new URL(addUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(query.getBytes(charset));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                    log.error("Error while closing the connection");
                }
            }
        }

        HttpURLConnection httpConn = (HttpURLConnection) connection;
        InputStream response;

        if (httpConn.getResponseCode() >= 400) {
            response = httpConn.getErrorStream();
        } else {
            response = connection.getInputStream();
        }

        String out = "{}";
        if (response != null) {
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = response.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
            }

            if (!sb.toString().trim().isEmpty()) {
                out = sb.toString();
            }
        }



        return out;
    }
    private boolean isValidJSON(String json) {

        try {
            new JSONObject(json);
            return true;
        } catch (JSONException ex) {
            return false;
        }
    }

    public static JSONObject sendRequest(String addUrl, String query) throws IOException, JSONException {

        String charset = "UTF-8";
        URLConnection connection = new URL(addUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(query.getBytes(charset));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                    log.error("Error while closing the connection");
                }
            }
        }

        HttpURLConnection httpConn = (HttpURLConnection) connection;
        InputStream response;

        if (httpConn.getResponseCode() >= 400) {
            response = httpConn.getErrorStream();
        } else {
            response = connection.getInputStream();
        }

        String out = "{}";
        if (response != null) {
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = response.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
            }

            if (!sb.toString().trim().isEmpty()) {
                out = sb.toString();
            }
        }

        JSONObject jsonObject = new JSONObject(out);

        return jsonObject;
    }

    public static OMElement sendXMLRequest(String addUrl, String query) throws MalformedURLException, IOException,
            XMLStreamException {

        String charset = "UTF-8";
        URLConnection connection = new URL(addUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(query.getBytes(charset));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                    log.error("Error while closing the connection");
                }
            }
        }

        HttpURLConnection httpConn = (HttpURLConnection) connection;
        InputStream response;

        if (httpConn.getResponseCode() >= 400) {
            response = httpConn.getErrorStream();
        } else {
            response = connection.getInputStream();
        }

        String out = "{}";
        if (response != null) {
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = response.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
            }

            if (!sb.toString().trim().isEmpty()) {
                out = sb.toString();
            }
        }

        OMElement omElement = AXIOMUtil.stringToOM(out);

        return omElement;

    }

    public static Properties getConnectorConfigProperties(String connectorName) {

        String connectorConfigFile = null;
        ProductConstant.init();
        try {
            connectorConfigFile =
                    ProductConstant.SYSTEM_TEST_SETTINGS_LOCATION + File.separator + "artifacts" + File.separator
                            + "ESB" + File.separator + "connector" + File.separator + "config" + File.separator
                            + connectorName + ".properties";
            File connectorPropertyFile = new File(connectorConfigFile);
            InputStream inputStream = null;
            if (connectorPropertyFile.exists()) {
                inputStream = new FileInputStream(connectorPropertyFile);
            }

            if (inputStream != null) {
                Properties prop = new Properties();
                prop.load(inputStream);
                inputStream.close();
                return prop;
            }

        } catch (IOException ignored) {
            log.error("automation.properties file not found, please check your configuration");
        }

        return null;
    }

    public static OMElement sendReceive(OMElement payload, String endPointReference, String operation,
                                        String contentType) throws AxisFault {

        ServiceClient sender;
        Options options;
        OMElement response = null;
        if (log.isDebugEnabled()) {
            log.debug("Service Endpoint : " + endPointReference);
            log.debug("Service Operation : " + operation);
            log.debug("Payload : " + payload);
        }
        try {
            sender = new ServiceClient();
            options = new Options();
            options.setTo(new EndpointReference(endPointReference));
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
            options.setTimeOutInMilliSeconds(45000);
            options.setAction("urn:" + operation);
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
            sender.setOptions(options);

            response = sender.sendReceive(payload);
            if (log.isDebugEnabled()) {
                log.debug("Response Message : " + response);
            }
        } catch (AxisFault axisFault) {
            log.error(axisFault.getMessage());
            throw new AxisFault("AxisFault while getting response :" + axisFault.getMessage(), axisFault);
        }
        return response;
    }
    private String readResponse(HttpURLConnection con) throws IOException {

        InputStream responseStream = null;
        String responseString = null;

        if (con.getResponseCode() >= 400) {
            responseStream = con.getErrorStream();
        } else {
            responseStream = con.getInputStream();
        }

        if (responseStream != null) {

            StringBuilder stringBuilder = new StringBuilder();
            byte[] bytes = new byte[1024];
            int len;

            while ((len = responseStream.read(bytes)) != -1) {
                stringBuilder.append(new String(bytes, 0, len));
            }

            if (!stringBuilder.toString().trim().isEmpty()) {
                responseString = stringBuilder.toString();
            }

        }

        return responseString;
    }

    /**
     * Method to read in contents of a file as String
     *
     * @param path
     * @return String contents of file
     * @throws java.io.IOException
     */
    public static String getFileContent(String path) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line = null;

            String ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

        } catch (IOException ioe) {
            log.error("Error reading request from file.", ioe);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return stringBuilder.toString();

    }

    /**
     * Convert first letter of a string to upper case
     *
     * @param string
     * @return <strong>String</strong> with the first letter as upper case
     */
    public static String firstToUpperCase(String string) {

        String post = string.substring(1, string.length());
        String first = ("" + string.charAt(0)).toUpperCase();
        return first + post;
    }

    /**
     * Inner class to handle Multipart data
     */
    public  class MultipartFormdataProcessor {

        private final String boundary = "----=_wso2IntegTest" + System.currentTimeMillis();

        OutputStream httpStream;

        HttpURLConnection httpURLConnection;

        final String LINE_FEED = "\r\n";

        public MultipartFormdataProcessor(String endPointUrl) throws IOException {

            init(endPointUrl, Charset.defaultCharset().toString(), null);
        }

        public MultipartFormdataProcessor(String endPointUrl, Map<String, String> httpHeaders) throws IOException {

            init(endPointUrl, Charset.defaultCharset().toString(), httpHeaders);
        }

        public MultipartFormdataProcessor(String endPointUrl, String charSet, Map<String, String> httpHeaders)
                throws IOException {

            init(endPointUrl, charSet, httpHeaders);
        }

        public MultipartFormdataProcessor(String endPointUrl, String Charset) throws IOException {

            init(endPointUrl, Charset, null);
        }

        public MultipartFormdataProcessor(String endPointUrl, Map<String, String> httpHeaders, int multipartType)
                throws IOException {

            init(endPointUrl, httpHeaders, multipartType);
        }

        private void init(String endPointUrl, String Charset, Map<String, String> httpHeaders) throws IOException {

            URL endpoint;

            endpoint = new URL(endPointUrl);

            httpURLConnection = (HttpURLConnection) endpoint.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "\"" + boundary
                    + "\"");
            httpURLConnection.setRequestProperty("User-Agent", "Wso2ESB intergration test");
            // httpURLConnection.setRequestProperty("Content-Length", 332);
            // check for custom headers

            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                Set<String> headerKeys = httpHeaders.keySet();
                String key = null;
                String value = null;
                for (Iterator<String> i = headerKeys.iterator(); i.hasNext();) {
                    key = i.next();
                    value = httpHeaders.get(key);
                    httpURLConnection.setRequestProperty(key, value);

                }
            }

            httpStream = httpURLConnection.getOutputStream();

        }

        private void init(String endPointUrl, Map<String, String> httpHeaders, int multipartType) throws IOException {

            URL endpoint;

            endpoint = new URL(endPointUrl);

            httpURLConnection = (HttpURLConnection) endpoint.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            switch (multipartType) {
                case MULTIPART_TYPE_RELATED:
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/related; boundary=" + "\""
                            + boundary + "\"");
                    break;
                default:
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "\""
                            + boundary + "\"");
            }

            httpURLConnection.setRequestProperty("User-Agent", "Wso2ESB intergration test");
            // httpURLConnection.setRequestProperty("Content-Length", 332);
            // check for custom headers

            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                // remove content type header as we have already set it
                httpHeaders.remove("Content-Type");
                Set<String> headerKeys = httpHeaders.keySet();
                String key = null;
                String value = null;
                for (Iterator<String> i = headerKeys.iterator(); i.hasNext();) {
                    key = i.next();
                    value = httpHeaders.get(key);
                    httpURLConnection.setRequestProperty(key, value);

                }
            }

            httpStream = httpURLConnection.getOutputStream();

        }
//
//        public void addMetadataToMultipartRelatedRequest(String filename, String contentType, String charset,
//                                                         Map<String, String> parametersMap) throws IOException {
//
//            StringBuilder builder = new StringBuilder();
//
//            builder.append(LINE_FEED);
//            builder.append("--").append(boundary).append(LINE_FEED);
//
//            builder.append("Content-Type: " + contentType + "; charset=" + charset).append(LINE_FEED).append(LINE_FEED);
//
//            builder.append(loadRequestFromFile(filename, parametersMap));
//
//            builder.append(LINE_FEED);
//
//            httpStream.write(builder.toString().getBytes());
//            httpStream.flush();
//        }
//
//        public void addFileToMultipartRelatedRequest(String fileName, String contentId) throws IOException {
//
//            File file = null;
//
//            InputStream inputStream = null;
//            try {
//
//                fileName = pathToResourcesDirectory + fileName;
//
//                file = new File(fileName);
//                String contentType;
//
//                inputStream = new FileInputStream(file);
//                contentType = HttpURLConnection.guessContentTypeFromName(fileName);
//                inputStream.close();
//
//                addFileToMultipartRelatedRequest(fileName, file, contentType, contentId);
//
//            } finally {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//
//            }
//        }
//
//        public void addFileToMultipartRelatedRequest(String fileName, File file, String contentType, String contentId)
//                throws IOException {
//
//            FileInputStream inputStream = null;
//            try {
//                StringBuilder builder = new StringBuilder();
//
//                builder.append("--").append(boundary).append(LINE_FEED);
//
//                builder.append("Content-Disposition: attachment; filename=\"" + fileName + "\"").append(LINE_FEED);
//
//                builder.append("Content-Type: " + contentType).append(LINE_FEED);
//                builder.append("content-id: <" + contentId + ">").append(LINE_FEED).append(LINE_FEED);
//
//                httpStream.write(builder.toString().getBytes());
//
//                httpStream.flush();
//
//                // process File
//                inputStream = new FileInputStream(file);
//                byte[] buffer = new byte[10485760];
//                int bytesRead = -1;
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    httpStream.write(buffer, 0, bytesRead);
//                }
//                httpStream.flush();
//                inputStream.close();
//
//            } finally {
//                if (inputStream != null)
//                    inputStream.close();
//            }
//        }
//
        public void addFormDataToRequest(String fieldName, String fieldValue) throws IOException {

            addFormDataToRequest(fieldName, fieldValue, Charset.defaultCharset().toString());
        }

        public void addFormDataToRequest(String fieldName, String fieldValue, String charset) throws IOException {

            StringBuilder builder = new StringBuilder();

            builder.append(LINE_FEED);
            builder.append("--").append(boundary).append(LINE_FEED);

            builder.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
            builder.append("Content-Disposition: form-data; name=\"" + fieldName + "\"").append(LINE_FEED);

            builder.append(LINE_FEED);
            builder.append(fieldValue).append(LINE_FEED);

            httpStream.write(builder.toString().getBytes());
            httpStream.flush();
        }
//
//        public void addFileToRequest(String fieldName, String fileName, String contentType) throws IOException {
//
//            fileName = pathToResourcesDirectory + fileName;
//            File file = new File(fileName);
//            addFileToRequest(fieldName, file, contentType);
//        }
//
        public void addFileToRequest(String fieldName, String fileName, String contentType, String targetFileName)
                throws IOException {
            String pathToFile=ProductConstant.SYSTEM_TEST_SETTINGS_LOCATION + File.separator + "artifacts" + File.separator
                    + "ESB" + File.separator + "config"+ File.separator + "resources" + File.separator + "teamwork" + File.separator+"a.txt";
           // fileName = pathToFile + fileName;
            File file = new File(pathToFile);
            if (contentType == null) {
                contentType = URLConnection.guessContentTypeFromName(file.getName());
            }
            addFileToRequest(fieldName, file, contentType, targetFileName);
        }
//
//        public void addFileToRequest(String fieldName, String fileName) throws IOException {
//
//            File file = null;
//
//            InputStream inputStream = null;
//            try {
//
//                fileName = pathToResourcesDirectory + fileName;
//
//                file = new File(fileName);
//                String contentType;
//
//                inputStream = new FileInputStream(file);
//                contentType = HttpURLConnection.guessContentTypeFromStream(inputStream);
//                inputStream.close();
//
//                addFileToRequest(fieldName, file, contentType);
//
//            } finally {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//
//            }
//        }
//
//        public void addFileToRequest(String fieldName, File file) throws IOException {
//
//            InputStream inputStream = null;
//            try {
//                String contentType;
//
//                inputStream = new FileInputStream(file);
//                contentType = HttpURLConnection.guessContentTypeFromStream(inputStream);
//                inputStream.close();
//
//                addFileToRequest(fieldName, file, contentType);
//
//            } finally {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//
//            }
//
//        }

        public void addFileToRequest(String fieldName, File file, String contentType, String fileName)
                throws IOException {

            FileInputStream inputStream = null;
            try {
                StringBuilder builder = new StringBuilder();

                builder.append("--").append(boundary).append(LINE_FEED);

                builder.append(
                        "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"")
                        .append(LINE_FEED);
                /*
                 * builder.append( "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" +
                 * "file" + "\"").append(LINE_FEED);
                 */
                builder.append("Content-Type: " + contentType).append(LINE_FEED);
                builder.append("Content-Transfer-Encoding: binary").append(LINE_FEED).append(LINE_FEED);

                httpStream.write(builder.toString().getBytes());

                httpStream.flush();

                // process File
                inputStream = new FileInputStream(file);
                byte[] buffer = new byte[10485760];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    httpStream.write(buffer, 0, bytesRead);
                }
                httpStream.flush();
                inputStream.close();
                httpStream.write(LINE_FEED.getBytes());
                httpStream.flush();

            } finally {
                if (inputStream != null)
                    inputStream.close();
            }

        }
//
//        public void addFileToRequest(String fieldName, File file, String contentType) throws IOException {
//
//            addFileToRequest(fieldName, file, contentType, file.getName());
//
//        }

//        public String processForStringResponse() throws IOException {
//
//            StringBuilder builder = new StringBuilder();
//
//            builder.append("--").append(boundary).append("--").append(LINE_FEED);
//            httpStream.write(builder.toString().getBytes());
//
//            httpStream.flush();
//
//            return readResponse(httpURLConnection);
//
//        }

        public RestResponse<JSONObject> processForJsonResponse() throws IOException, JSONException {

            StringBuilder builder = new StringBuilder();

            builder.append("--").append(boundary).append("--").append(LINE_FEED);
            httpStream.write(builder.toString().getBytes());

            httpStream.flush();

            String responseString = readResponse(httpURLConnection);
            RestResponse<JSONObject> restResponse = new RestResponse<JSONObject>();
            restResponse.setHttpStatusCode(httpURLConnection.getResponseCode());
            restResponse.setHeadersMap(httpURLConnection.getHeaderFields());

            if (responseString != null) {
                JSONObject jsonObject = null;
                if (isValidJSON(responseString)) {
                    jsonObject = new JSONObject(responseString);
                } else {
                    jsonObject = new JSONObject();
                    jsonObject.put("output", responseString);
                }

                restResponse.setBody(jsonObject);
            }
            return restResponse;

        }

//        public RestResponse<OMElement> processForXmlResponse() throws IOException, XMLStreamException {
//
//            StringBuilder builder = new StringBuilder();
//
//            builder.append("--").append(boundary).append("--").append(LINE_FEED);
//            httpStream.write(builder.toString().getBytes());
//
//            httpStream.flush();
//
//            String responseString = readResponse(httpURLConnection);
//            RestResponse<OMElement> restResponse = new RestResponse<OMElement>();
//            restResponse.setHttpStatusCode(httpURLConnection.getResponseCode());
//            restResponse.setHeadersMap(httpURLConnection.getHeaderFields());
//
//            if (responseString != null) {
//                restResponse.setBody(AXIOMUtil.stringToOM(responseString));
//            }
//
//            return restResponse;
//
//        }

//        public void addFiletoRequestBody(File file) throws IOException {
//
//            FileInputStream inputStream = null;
//            try {
//
//                // process File
//                inputStream = new FileInputStream(file);
//                byte[] buffer = new byte[4096];
//                int bytesRead = -1;
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    httpStream.write(buffer, 0, bytesRead);
//                }
//                httpStream.flush();
//                inputStream.close();
//
//            } finally {
//                if (inputStream != null)
//                    inputStream.close();
//            }
//        }
//
//        public void addChunckedFiletoRequestBody(byte[] bytesPortion) throws IOException {
//
//            httpStream.write(bytesPortion);
//
//            httpStream.flush();
//
//        }

//        public RestResponse<JSONObject> processAttachmentForJsonResponse() throws IOException, JSONException {
//
//            String responseString = readResponse(httpURLConnection);
//            RestResponse<JSONObject> restResponse = new RestResponse<JSONObject>();
//            restResponse.setHttpStatusCode(httpURLConnection.getResponseCode());
//            restResponse.setHeadersMap(httpURLConnection.getHeaderFields());
//
//            if (responseString != null) {
//                JSONObject jsonObject = null;
//                if (isValidJSON(responseString)) {
//                    jsonObject = new JSONObject(responseString);
//                } else {
//                    jsonObject = new JSONObject();
//                    jsonObject.put("output", responseString);
//                }
//
//                restResponse.setBody(jsonObject);
//            }
//            return restResponse;
//
//        }

//        public RestResponse<OMElement> processAttachmentForXmlResponse() throws IOException, XMLStreamException {
//
//            final String responseString = readResponse(httpURLConnection);
//            final RestResponse<OMElement> restResponse = new RestResponse<OMElement>();
//            restResponse.setHttpStatusCode(httpURLConnection.getResponseCode());
//            restResponse.setHeadersMap(httpURLConnection.getHeaderFields());
//
//            if (responseString != null) {
//                restResponse.setBody(AXIOMUtil.stringToOM(responseString));
//            }
//
//            return restResponse;
//
//        }
//
    }

}
