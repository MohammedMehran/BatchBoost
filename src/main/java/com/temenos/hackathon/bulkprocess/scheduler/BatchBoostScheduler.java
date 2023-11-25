package com.temenos.hackathon.bulkprocess.scheduler;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * TODO: Document me!
 *
 * @author mohammed.mehran
 *
 */

@Service
@EnableScheduling
public class BatchBoostScheduler {

    public static Logger logger = LogManager.getLogger(BatchBoostScheduler.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Qualifier("BatchBoostJob")
    @Autowired
    private Job batchBoostJob;
    
    
    @Qualifier("TransferFundJob")
    @Autowired
    private Job transferFundJob;

    @Value("${filepath}")
    private String filePath;

    @Scheduled(cron = "${hacker.cron:*/10 * * * * *}")
    public void batchBoostScheduler() {

        try {

            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("file:" + filePath + "HackerFile*.csv");

            for (Resource file : resources) {

                String filename = file.getFilename();

                logger.info("Running Batch Boost Scheduler for the file " + filename + "...");

                JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                        .addString("currentFile", filename).addString("filePath", file.getURI().toString())
                        .toJobParameters();

                try {
                    JobExecution jobExecution = jobLauncher.run(batchBoostJob, jobParameters);

                    logger.info("Job start time ::: " + jobExecution.getStartTime().toString() + " | Job Id ::: "
                            + jobExecution.getJobId() + " | Job's Status ::: " + jobExecution.getStatus()
                            + " | Job end time ::: " + jobExecution.getEndTime().toString());

                } catch (Exception e) {
                    logger.debug("Exception while running BatchBoostJob.", e);

                }

            }

        } catch (IOException e) {
            logger.debug("Exception during fetching the Hacker resources.", e);
        }

    }
    
   @Scheduled(cron = "${hacker.cron:*/50 * * * * *}")
    public void transferFunds() {

        logger.info("\n\n\n Running Transfer funds Job...");

        try {

            JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();

            JobExecution jobExecution = jobLauncher.run(transferFundJob, jobParameters);

            logger.info("Job start time ::: " + jobExecution.getStartTime().toString() + " | Job Id ::: "
                    + jobExecution.getJobId() + " | Job's Status ::: " + jobExecution.getStatus()
                    + " | Job end time ::: " + jobExecution.getEndTime().toString());

        } catch (Exception e) {
            logger.debug("Exception while running Transfer funds Job....", e);

        }

    }

}
