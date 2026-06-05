package pl.autopilot.datacollector;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DataCollectorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataCollectorApplication.class, args);
    }
}
