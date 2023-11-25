package com.temenos.hackathon.bulkprocess.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.temenos.hackathon.bulkprocess.model.Hacker;
import com.temenos.hackathon.bulkprocess.service.RestApi;

/**
 *
 * @author mohammed.mehran
 *
 */
@Component("HackerWriter")
public class HackerWriter implements ItemWriter<Hacker> {

    public static Logger logger = LogManager.getLogger(HackerWriter.class);

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Value("${enable.threading:No}")
    private String enableThreading;

    @Value("${create.hackers:http://localhost:9089/hackathon/api/v1.0.0/party/createHackers/}")
    private String createHackersPath;

    @Autowired
    private RestApi restApi;

    @Override
    public void write(List<? extends Hacker> items) throws Exception {

        if (enableThreading.equalsIgnoreCase("Yes")) {

            List<CompletableFuture<Void>> asyncRequests = new ArrayList<>();

            for (Hacker hacker : items) {

                createHackers(hacker, asyncRequests);

            }

            // Wait for all asynchronous requests to complete.
            CompletableFuture<Void>[] requestArray = asyncRequests.toArray(new CompletableFuture[asyncRequests.size()]);

            CompletableFuture<Void> allOf = CompletableFuture.allOf(requestArray);

            try {
                allOf.get(); // This will block until all requests are
                             // completed.
            } catch (InterruptedException | ExecutionException e) {
                logger.debug("Exception occured in ExportMarginService > executeHTTPCall : " + e.getLocalizedMessage(),
                        e);
            }

        } else {

            for (Hacker hacker : items) {

                createHackers(hacker);

            }
        }

    }

    /**
     * @param hacker
     */
    private void createHackers(Hacker hacker) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        String jsonRequestBody = "";

        String recordId = hacker.getEmployeeId();

        String URL = createHackersPath + recordId;

        jsonRequestBody = formRequestBody(hacker, objectMapper, jsonRequestBody, recordId);

        executeHTTPCall(jsonRequestBody, URL);

    }

    /**
     * @param hacker
     * @param asyncRequests
     */
    private void createHackers(Hacker hacker, List<CompletableFuture<Void>> asyncRequests) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        String jsonRequestBody = "";

        String recordId = hacker.getEmployeeId();

        String URL = createHackersPath + recordId;

        jsonRequestBody = formRequestBody(hacker, objectMapper, jsonRequestBody, recordId);

        excecuteAsyncHTTPCall(jsonRequestBody, URL, asyncRequests);

    }

    /**
     * @param hacker
     * @param objectMapper
     * @param jsonRequestBody
     * @param recordId
     * @return
     */
    private String formRequestBody(Hacker hacker, ObjectMapper objectMapper, String jsonRequestBody, String recordId) {
        try {

            // Set global configuration options
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

            hacker.setCouponTransfered("NO");

            JsonNode jsonNode = objectMapper.valueToTree(hacker);

            ObjectNode objectNode = (ObjectNode) jsonNode;

            objectNode.remove("employeeId");

            ObjectNode newNode = objectMapper.createObjectNode();
            newNode.set("body", objectNode);

            jsonRequestBody = objectMapper.writeValueAsString(newNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonRequestBody;
    }

    /**
     * @param jsonRequestBody
     * @param uRL
     */
    public void executeHTTPCall(String jsonRequestBody, String URL) {

        logger.info("Request URL : " + URL);
        logger.info("Request Body : " + jsonRequestBody);

        restApi.executePostApi(jsonRequestBody, URL);

    }

    /**
     * @param jsonRequestBody
     * @param uRL
     * @param asyncRequests
     */
    public void excecuteAsyncHTTPCall(String requestBody, String URL, List<CompletableFuture<Void>> asyncRequests) {

        logger.info("Request URL : " + URL);
        logger.info("Request Body : " + requestBody);

        CompletableFuture<Void> asyncRequest = CompletableFuture.runAsync(() -> {

            restApi.executePostApi(requestBody, URL);

        }, taskExecutor);

        asyncRequests.add(asyncRequest);

    }

}
