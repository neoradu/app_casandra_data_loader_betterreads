package com.radu.betterreads.bootstrap;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.radu.betterreads.data.Author;
import com.radu.betterreads.repositories.AuthorRepository;

@Component
public class DataBootstrap implements CommandLineRunner {
    Logger log = LoggerFactory.getLogger(DataBootstrap.class);
    final private AuthorRepository authorRepository;
    
    
    public DataBootstrap(AuthorRepository authorRepository) {
        super();
        this.authorRepository = authorRepository;
    }
    
    public void bootstrapAuthors() {
        authorRepository.deleteAll();
        Author a0 = new Author(UUID.randomUUID().toString(), "Vasile Andrei");
        Author a1 = new Author(UUID.randomUUID().toString(), "Gigel Andrei");
        a0 = authorRepository.save(a0);
        a1 = authorRepository.save(a1);
        
        log.debug(String.format("Author:%s saved id:%s", a0.getName(), a0.getId()));
        log.debug(String.format("Author:%s saved id:%s", a1.getName(), a1.getId()));     
       
    }
    
    @Override
    public void run(String... args) throws Exception {
        bootstrapAuthors();
        
    }

}
