package com.radu.betterreads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ShutDownManager {
    Logger log = LoggerFactory.getLogger(ShutDownManager.class);
    @Autowired
    private ApplicationContext appContext;
    
    public void initiateShutdown(int returnCode) {
        log.info("Shutdown...");
        SpringApplication.exit(appContext, () -> returnCode);
    }
    
}
