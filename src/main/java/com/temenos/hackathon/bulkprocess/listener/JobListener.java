package com.temenos.hackathon.bulkprocess.listener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class JobListener implements JobExecutionListener {

    @Value("${archivalPath}")
    private String archivalPath;

    @Value("${filepath}")
    private String filePath;

    public static Logger logger = LogManager.getLogger(JobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {

            String fileName = jobExecution.getJobParameters().getString("currentFile");
            Resource resource = new FileSystemResource(filePath + fileName);

            if (!resource.exists()) {
                throw new FileNotFoundException("Resource is missing :: " + filePath + fileName);

            }

            String uri = "";

            try {
                uri = resource.getURI().getPath();

            } catch (IOException e) {
                throw new IOException(
                        "Exception occured while fetching the path for the resource : " + resource.getFilename(), e);
            }

            uri = uri.substring(1);

            Path temp = null;

            try {
                temp = Files.move(Paths.get(uri), Paths.get(archivalPath + resource.getFilename()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // e.printStackTrace();
                throw new IOException("Exception occured while moving the resource : " + resource.getFilename()
                        + " to the path : " + archivalPath, e);
            }

            if (temp != null) {
                logger.info("File moved successfully to path : " + archivalPath + resource.getFilename());
            } else {

                logger.info("Failed to move the file !!");
            }

        } catch (FileNotFoundException e) {
            logger.error("Exception occurred while archiving the file ::: ", e);

        } catch (IOException e) {
            logger.error("Exception occurred while archiving the file ::: ", e);
        } catch (Exception e) {
            logger.error("Exception occurred while archiving the file ::: ", e);
        }

    }
}
