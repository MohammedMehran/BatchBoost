package com.temenos.hackathon.bulkprocess.app;

import java.util.Properties;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BatchBoostApplication.class).properties(getProperties());
    }
    
    static Properties getProperties() {
          Properties props = new Properties();
          props.put("spring.config.location", "classpath:springProperties/");
          return props;
       }


}
