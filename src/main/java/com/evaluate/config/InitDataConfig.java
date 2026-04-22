package com.evaluate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitDataConfig implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("InitDataConfig is running (no-op)");
    }
}
