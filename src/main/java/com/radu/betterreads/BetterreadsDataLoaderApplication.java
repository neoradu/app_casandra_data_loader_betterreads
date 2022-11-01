package com.radu.betterreads;

import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.radu.betterreads.connection.DataStraxAstraProperties;

@SpringBootApplication
@EnableConfigurationProperties(DataStraxAstraProperties.class)
public class BetterreadsDataLoaderApplication {
    
    //@Bean
    /*public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStraxAstraProperties astraProperties) {
        Path bundle = astraProperties.getSecureConnectBundle().toPath();
        return builder -> builder.withCloudSecureConnectBundle(bundle)
                                 .withAuthCredentials("qtNNcppQJJjmSFcAIKpUKliz", 
                                         "Z0osN25fHi3f3OyKNmC+iKhBbPOHuqOtkK9veoMDd4ljqd,BIOnmt9pi_0hE6Nwf-xSwNQwhjanMfrXdtO8bxjS7b9CZl0Ei4TPzT3ESJUAn.m,9NZIOvsRJIEAUjlay");
    }*/
    
	public static void main(String[] args) {
		SpringApplication.run(BetterreadsDataLoaderApplication.class, args);
	}

}
