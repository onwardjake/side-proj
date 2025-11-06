package com.jake.landtrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

//@SpringBootApplication
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ConfigurationPropertiesScan
public class LandTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LandTradeApplication.class, args);
    }

}
