package com.app.melashor;

import com.app.melashor.config.FeedAsyncProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties(FeedAsyncProperties.class)
public class MelashorApplication {

    static void main(String[] args) {
        SpringApplication.run(MelashorApplication.class, args);
    }

}
