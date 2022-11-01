package com.radu.betterreads.connection;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*These is not used*/
//@ConfigurationProperties(prefix = "datastax.astra")
public class DataStraxAstraProperties {
    private File secureConnectBundle;

    public File getSecureConnectBundle() {
        return secureConnectBundle;
    }

    public void setSecureConnectBundle(File secureConnectBundle) {
        this.secureConnectBundle = secureConnectBundle;
    }
    
}
