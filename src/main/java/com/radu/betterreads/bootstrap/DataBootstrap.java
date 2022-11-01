package com.radu.betterreads.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radu.betterreads.ShutDownManager;
import com.radu.betterreads.data.Author;
import com.radu.betterreads.repositories.AuthorRepository;

@Component
public class DataBootstrap implements CommandLineRunner {
    Logger log = LoggerFactory.getLogger(DataBootstrap.class);
    final private AuthorRepository authorRepository;
    final private ShutDownManager shutdownManager;
    
    @Value("${dataloader.data.authors.path}")
    private String authorsFilePath;

    @Value("${dataloader.data.books.path}")
    private String booksFilePath;
    
    public DataBootstrap(AuthorRepository authorRepository, ShutDownManager shutdownManager) {
        super();
        this.authorRepository = authorRepository;
        this.shutdownManager = shutdownManager;
    }
    
    public void bootstrapAuthors() {
        authorRepository.deleteAll();
        
        if(authorsFilePath == null)
            throw new RuntimeException("Authors file not found");
        
        Path path = Paths.get(authorsFilePath);
        
        //try with resource --> it will close the resource at the end
        try(Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                
                try {
                    String jsonString = line.substring(line.indexOf('{'));
                    JsonNode jsonNode = (new ObjectMapper().readTree(jsonString));
                    Author author = new Author();
                    author.setId(jsonNode.get("key").asText().replace("/authors/", ""));
                    author.setName(jsonNode.path("name").asText("NA"));
                    author.setPersonalName(jsonNode.path("personal_name").asText("NA"));
                    author = authorRepository.save(author);
                    log.debug(String.format("Author:%s saved id:%s", author.getName(), author.getId()));
                } catch (JsonMappingException e) {} 
                  catch (JsonProcessingException e) {}
            });
        } catch (IOException e) {
            e.printStackTrace();
            new RuntimeException("Exception when reding the authors file", e);
        }     
    }
    
    @Override
    public void run(String... args) throws Exception {
        bootstrapAuthors();
        shutdownManager.initiateShutdown(0);
    }

}
