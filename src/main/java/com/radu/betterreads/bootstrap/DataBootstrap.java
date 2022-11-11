package com.radu.betterreads.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
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
import com.radu.betterreads.data.Book;
import com.radu.betterreads.repositories.AuthorRepository;
import com.radu.betterreads.repositories.BookRepository;

@Component
public class DataBootstrap implements CommandLineRunner {
    Logger log = LoggerFactory.getLogger(DataBootstrap.class);
    final private AuthorRepository authorRepository;
    final private ShutDownManager shutdownManager;
    final private BookRepository bookRepository;
    
    @Value("${dataloader.data.authors.path}")
    private String authorsFilePath;

    @Value("${dataloader.data.books.path}")
    private String booksFilePath;
    
    public DataBootstrap(AuthorRepository authorRepository, BookRepository bookRepository,
                         ShutDownManager shutdownManager) {
        this.authorRepository = authorRepository;
        this.shutdownManager = shutdownManager;
        this.bookRepository = bookRepository;
    }
    
    public void bootstrapAuthors() { 
        
        if(authorsFilePath == null) {
            log.error("Authors file path not found!");
            return;
        } else {
            log.debug("Opening authors file path:" + authorsFilePath);
        }
        Instant startTime = Instant.now();
        
        //try with resource --> it will close the resource at the end
        try(Stream<String> lines = Files.lines(Paths.get(authorsFilePath))) {
            lines
                 //.limit(10000)
                 .parallel()
                 .forEach(line -> {
                try {
                    String jsonString = line.substring(line.indexOf('{'));
                    JsonNode jsonNode = (new ObjectMapper().readTree(jsonString));
                    Author author = new Author();
                    author.setId(jsonNode.path("key").asText("dummy_id").replace("/authors/", ""));
                    author.setName(jsonNode.path("name").asText("NA"));
                    author.setPersonalName(jsonNode.path("personal_name").asText("NA"));
                    author = authorRepository.save(author);

                    /*log.debug(String.format("ThreadAuthor:%d saved author:%s id:%s",
                                             Thread.currentThread().getId(), author.getName(), author.getId()));*/
                } catch (JsonMappingException e) {} 
                  catch (JsonProcessingException e) {}
            });
        } catch (IOException e) {
            e.printStackTrace();
            new RuntimeException("Exception when reding the authors file", e);
        } 
        log.debug(String.format("Authors Loaded in: %dmin %ds %dms", Duration.between(startTime, Instant.now()).toMinutes(),
                Duration.between(startTime, Instant.now()).toSecondsPart(),
                Duration.between(startTime, Instant.now()).toMillisPart()));
    }
    
    public void bootstrapBooks() {
        
        if(booksFilePath == null) {
            log.error("Books file path not found!");
            return;
        } else {
            log.debug("Opening Books file path:" + booksFilePath);
        }
        Instant startTime = Instant.now();
        //try with resource --> it will close the resource at the end
        try(Stream<String> lines = Files.lines(Paths.get(booksFilePath))) {
            final Author defaultAuthor = new Author("dummy", "Unknown Author");
            lines
            //.limit(100000)
            .parallel()
            .forEach(line -> {
                try {
                    String jsonString = line.substring(line.indexOf('{'));
                    JsonNode bookJson = (new ObjectMapper().readTree(jsonString));
                    Book book = new Book();
                    book.setId(bookJson.get("key").asText("default_id").replace("/works/", ""));
                    book.setName(bookJson.path("title").asText("NA").trim());
                    String dateString = bookJson.path("created").path("value").asText("1900-01-01T");
                    dateString = dateString.substring(0, dateString.indexOf("T"));
                    book.setPublishedDate(LocalDate.parse(dateString));
                    if(bookJson.path("authors").isArray()) {
                        for(JsonNode authorJson : bookJson.get("authors")) {
                            String authorId = authorJson.path("author").path("key").asText(null);
                            Author author = defaultAuthor;
                            if(authorId != null) {
                                authorId = authorId.replace("/authors/", "");
                                Optional<Author> authorOpt = authorRepository.findById(authorId);
                                author = authorOpt.orElse(defaultAuthor);
                            }
                            book.getAuthorNames().add(author.getName());
                            book.getAuthorIds().add(author.getId());
                        }
                    }
                    book.getAuthorNames().add("Unknown Author");
                    book.getAuthorIds().add("dummy_id");
                    
                    if(bookJson.path("covers").isArray()) { 
                        for(JsonNode cover : bookJson.get("covers")) {
                            book.getCoverIds().add(cover.asText());
                        }
                    }
                    book.setDescription(bookJson.path("description").path("value").asText("NA"));
                    
                    book = bookRepository.save(book);

                    /*log.debug(String.format("Thread:%d Book:\"%s\" author:%s -> saved id:%s",
                                             Thread.currentThread().getId(),
                                             book.getName(),book.getAuthorNames(), book.getId()));*/
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 

            });
            
        } catch (IOException e) {
            e.printStackTrace();
            new RuntimeException("Exception when reding the authors file", e);
        }   
        log.debug(String.format("Books Loaded in: %dmin %ds %dms", Duration.between(startTime, Instant.now()).toMinutes(),
                                                                  Duration.between(startTime, Instant.now()).toSecondsPart(),
                                                                  Duration.between(startTime, Instant.now()).toMillisPart()));
    }
    
    @Override
    public void run(String... args) throws Exception {
        
        bootstrapAuthors();
        bootstrapBooks();

        shutdownManager.initiateShutdown(0);
    }

}
