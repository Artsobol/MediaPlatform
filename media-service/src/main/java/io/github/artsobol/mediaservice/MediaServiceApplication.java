package io.github.artsobol.mediaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MediaServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }

}
