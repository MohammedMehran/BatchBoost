package com.temenos.hackathon.bulkprocess.config;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.temenos.hackathon.bulkprocess.listener.BatchBoostSkipListener;
import com.temenos.hackathon.bulkprocess.listener.JobListener;
import com.temenos.hackathon.bulkprocess.model.Hacker;
import com.temenos.hackathon.bulkprocess.service.RestApi;
import com.temenos.hackathon.bulkprocess.writer.HackerWriter;

/***
 * @author mohammed.mehran
 *
 */

@Configuration
public class BatchBoostConfig {

    public static Logger logger = LogManager.getLogger(BatchBoostConfig.class);

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(20); // Number of threads to keep in the
                                          // pool
        taskExecutor.setMaxPoolSize(30); // Maximum number of threads
        taskExecutor.setQueueCapacity(100); // Maximum number of tasks that can
                                            // be queued
        taskExecutor.setThreadNamePrefix("BatchBoostThread");
        taskExecutor.initialize();

        return taskExecutor;
    }

    @Bean
    public JobLauncher jobLauncher(ThreadPoolTaskExecutor taskExecutor, JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setTaskExecutor(taskExecutor());
        jobLauncher.setJobRepository(jobRepository);
        return jobLauncher;
    }

    @Value("${filepath}")
    private String filePath;

    @Value("${archivalPath}")
    private String archivalPath;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobListener jobListener;

    @Value("${delimiter:,}")
    private String delimiter;

    @Autowired
    @Qualifier("BatchBoostSkipListener")
    private BatchBoostSkipListener batchBoostSkipListener;

    @Autowired
    @Qualifier("HackerWriter")
    private HackerWriter hackerWriter;

    @Autowired
    private RestApi restApi;

    @Bean("BatchBoostJob")
    public Job BatchBoostJob() throws Exception {

        logger.info("Running Batch Boost Job...");

        return jobBuilderFactory.get("Batch Boost Job").incrementer(new RunIdIncrementer()).start(batchBoostStep())
                .listener(jobListener).build();
    }

    /**
     * @return
     */
    private Step batchBoostStep() {

        logger.info("Running Batch Boost Step...");

        return stepBuilderFactory.get("batchBoostStep").<Hacker, Hacker>chunk(20)
                .reader(batchBoostFlatFileItemReader(null)).writer(hackerWriter).faultTolerant().skip(Throwable.class)
                .skipPolicy(new AlwaysSkipItemSkipPolicy()).listener(batchBoostSkipListener).build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Hacker> batchBoostFlatFileItemReader(
            @Value("#{jobParameters['currentFile']}") String fileName) {

        FlatFileItemReader<Hacker> flatFileItemReader = new FlatFileItemReader<Hacker>();

        FileSystemResource resource = new FileSystemResource(new File(filePath + fileName));

        flatFileItemReader.setResource(resource);

        // Configure the line mapping
        flatFileItemReader.setLineMapper(new DefaultLineMapper<Hacker>() {
            {
                // Configure the line tokenizer for CSV format

                setLineTokenizer(new DelimitedLineTokenizer(delimiter) {
                    {
                        // Set the names of columns in the CSV file
                        setNames("employeeId", "employeeName", "department", "mobileNumber", "email", "accountNo");
                    }
                });

                // Configure the field set mapper
                setFieldSetMapper(new BeanWrapperFieldSetMapper<Hacker>() {
                    {
                        // Set the target type for mapping
                        setTargetType(Hacker.class);
                    }
                });
            }
        });

        // Skip the header line
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;

    }

    @Bean("TransferFundJob")
    public Job TransferFundJob() throws Exception {

        logger.info("Running Transfer Fund Job...");

        return jobBuilderFactory.get("Transfer Fund Job").start(transferFundStep()).build();

    }

    /**
     * @return
     */
    private Step transferFundStep() {

        logger.info("Running TransferFundStep...");

        return stepBuilderFactory.get("First Step").tasklet(firstTask()).build();
    }

    private Tasklet firstTask() {
        return new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

                List<Hacker> hackerList = restApi.getHackersList();

                restApi.createFundTransfer(hackerList);

                return RepeatStatus.FINISHED;
            }
        };
    }

}
