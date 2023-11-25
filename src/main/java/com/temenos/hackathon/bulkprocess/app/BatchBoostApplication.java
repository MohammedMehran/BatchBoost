package com.temenos.hackathon.bulkprocess.app;

import java.util.Properties;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "com.temenos.hackathon.bulkprocess" })
@EnableBatchProcessing
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@ImportResource({ "classpath:applicationContext/Hackathon.xml" })
public class BatchBoostApplication {

	public static void main(String[] args) {
	    ConfigurableApplicationContext springApplicationBuilder = new SpringApplicationBuilder(
	            BatchBoostApplication.class).sources(BatchBoostApplication.class).properties(getProperties())
                        .run(args);
    }

    static Properties getProperties() {
        Properties props = new Properties();
        props.put("spring.config.location", "classpath:springProperties/");
        return props;
    }

}
