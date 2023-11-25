package com.temenos.hackathon.bulkprocess.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.temenos.hackathon.bulkprocess.model.Hacker;
import com.temenos.hackathon.bulkprocess.writer.HackerWriter;

/**
 *
 * @author mohammed.mehran
 *
 */
@Component
public class RestApi {

    public static Logger logger = LogManager.getLogger(RestApi.class);

    @Value("${create.funds.transfer:http://localhost:9089/hackathon/api/v1.0.0/party/createAccountTransfers}")
    private String createFundsTransfer;

    @Value("${create.hackers:http://localhost:9089/hackathon/api/v1.0.0/party/createHackers/}")
    private String createHackersPath;

    @Value("${get.all.hackers:http://localhost:9089/hackathon/api/v1.0.0/party/getHackers}")
    private String getAllHackers;

    @Autowired
    public HackerWriter hackerWriter;

    public RestApi() {

    }

    public List<Hacker> getHackersList() {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(getAllHackers);

        List<Hacker> hackerList = new ArrayList<>();

        try {

            CloseableHttpResponse response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseContent = EntityUtils.toString(response.getEntity());

            if (statusCode == 200) {

                try {
                    // Create ObjectMapper instance
                    ObjectMapper objectMapper = new ObjectMapper();

                    // Parse JSON string
                    JsonNode jsonNode = objectMapper.readTree(responseContent);

                    // Get the "body" array
                    JsonNode bodyArray = jsonNode.get("body");

                    // Loop through the "body" array
                    for (JsonNode hackerNode : bodyArray) {
                        Hacker hacker = objectMapper.treeToValue(hackerNode, Hacker.class);
                        hackerList.add(hacker);
                    }

                    return hackerList;
                } catch (Exception e) {
                    logger.debug("Exception occured in RestApi > getHackersList : " + e.getLocalizedMessage(), e);
                }

            } else {

                logger.info("Request was not successful (fetchURLContent). Response: " + responseContent);
            }
        } catch (Exception e) {
            logger.debug("Exception occured in RestApi > getHackersList : " + e.getLocalizedMessage(), e);
        } finally {
            try {

                httpClient.close();
            } catch (Exception e) {
                logger.debug("Exception occured in RestApi > getHackersList : " + e.getLocalizedMessage(), e);
            }

        }

        return hackerList;
    }

    public void createFundTransfer(List<Hacker> hackerList) {

        for (Hacker hacker : hackerList) {

            if (hacker == null || hacker.getAccountNo() == null || hacker.getAccountNo().isEmpty()) {
                continue;
            }

            String requestBody = formFundTransferBody(hacker);

            List<String> responseList = executePostApi(requestBody, createFundsTransfer);

            try {
                int statusCode = Integer.parseInt(responseList.get(0));

                if (statusCode >= 200 && statusCode < 300) {

                    logger.info("Request was successful. Response: " + responseList.get(1));

                    String recordId = hacker.getEmployeeId();

                    String URL = createHackersPath + recordId;

                    requestBody = updateCoupon(hacker);

                    hackerWriter.executeHTTPCall(requestBody, URL);

                } else {

                    logger.info("Request was not successful. Response: " + responseList.get(1));

                }
            } catch (Exception e) {
                logger.debug("Exception occured in RestApi > createFundTransfer : " + e.getLocalizedMessage(), e);
            }

        }

    }

    /**
     * @param hacker
     * @return
     */
    private String updateCoupon(Hacker hacker) {

        String jsonRequest = null;

        try {
            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Create JSON structure
            ObjectNode requestBody = objectMapper.createObjectNode();
            ObjectNode bodyNode = objectMapper.createObjectNode();

            bodyNode.put("couponTransfered", "YES");

            requestBody.set("body", bodyNode);

            // Convert to JSON string
            jsonRequest = objectMapper.writeValueAsString(requestBody);

        } catch (Exception e) {
            logger.debug("Exception occured in RestApi > updateCoupon : " + e.getLocalizedMessage(), e);
        }

        return jsonRequest;
    }

    /**
     * @param hacker
     * @return
     */
    private String formFundTransferBody(Hacker hacker) {

        String jsonRequest = null;

        try {
            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Create JSON structure
            ObjectNode requestBody = objectMapper.createObjectNode();
            ObjectNode bodyNode = objectMapper.createObjectNode();

            bodyNode.put("transactionType", "AC");
            bodyNode.put("debitAccountId", "100064");
            bodyNode.put("debitCurrency", "USD");
            bodyNode.put("debitAmount", "250");
            bodyNode.put("creditAccountId", hacker.getAccountNo());
            bodyNode.put("ftTemHack", "TEMENOS_HACK");

            requestBody.set("body", bodyNode);

            // Convert to JSON string
            jsonRequest = objectMapper.writeValueAsString(requestBody);

        } catch (Exception e) {
            logger.debug("Exception occured in RestApi > formFundTransferBody : " + e.getLocalizedMessage(), e);
        }

        return jsonRequest;
    }

    /**
     * @param jsonRequestBody
     * @param uRL
     */
    public List<String> executePostApi(String requestBody, String URL) {

        List<String> result = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(URL);

            // Set headers if needed
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity requestEntity = new StringEntity(requestBody);
            httpPost.setEntity(requestEntity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // Handle response
                int statusCode = response.getStatusLine().getStatusCode();
                result.add(String.valueOf(statusCode));

                String responseBody = "";

                HttpEntity responseEntity = response.getEntity();

                if (responseEntity != null) {
                    responseBody = EntityUtils.toString(responseEntity);

                    result.add(responseBody);

                }

                if (statusCode >= 200 && statusCode < 300) {

                    logger.info("Request was successful. Response: " + responseBody);

                } else {

                    logger.info("Request was not successful. Response: " + responseBody);
                    logger.error("Request was not successful. Response: " + responseBody);
                }

                EntityUtils.consume(responseEntity);

            }

        } catch (Exception e) {
            logger.debug("Exception occured in RestApi > executePostApi : " + e.getLocalizedMessage(), e);
        }

        return result;

    }

}
