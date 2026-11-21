package com.lucas.pacioli_ledger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class PacioliLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PacioliLedgerApplication.class, args);
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment environment = event.getApplicationContext().getEnvironment();

        String applicationName = environment.getProperty(
                "spring.application.name",
                "application"
        );

        String[] activeProfiles = environment.getActiveProfiles();

        log.info(
                "Application '{}' is ready. Active profiles: {}",
                applicationName,
                activeProfiles.length > 0 ?
                        String.join(", ", activeProfiles) : "default"
        );
    }
}
