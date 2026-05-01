package com.example.schedulemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.sentry.Sentry;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ScheduleManagerApplication {

    public static void main(String[] args) {

        Sentry.init(options -> {
            options.setDsn("https://9e78eb7159829f55271214631ef7e1b1@o4511313079894016.ingest.us.sentry.io/4511313098833920");
        });

        SpringApplication.run(ScheduleManagerApplication.class, args);
    }
}