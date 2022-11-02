package com.radu.betterreads.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
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
        authorRepository.deleteAll();
        
        if(authorsFilePath == null)
            throw new RuntimeException("Authors file not found");
        
        //try with resource --> it will close the resource at the end
        try(Stream<String> lines = Files.lines(Paths.get(authorsFilePath))) {
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
    
    public void bootstrapBooks() {
        bookRepository.deleteAll();
        
        if(booksFilePath == null)
            throw new RuntimeException("Books file not found");
        //try with resource --> it will close the resource at the end
        try(Stream<String> lines = Files.lines(Paths.get(booksFilePath))) {
            lines.forEach(line -> {
                try {
                    String jsonString = line.substring(line.indexOf('{'));
                    JsonNode bookJson = (new ObjectMapper().readTree(jsonString));
                    Book book = new Book();
                    book.setId(bookJson.get("key").asText().replace("/works/", ""));
                    book.setName(bookJson.path("title").asText("NA").trim());
                    String dateString = bookJson.path("created").path("value").asText("1900-01-01T");
                    dateString = dateString.substring(0, dateString.indexOf("T"));
                    book.setPublishedDate(LocalDate.parse(dateString));
                    if(bookJson.path("authors").isArray()) {
                        for(JsonNode authorJson : bookJson.get("authors")) {
                            String authorId = authorJson.get("author").get("key").asText().replace("/authors/", "");
                            Optional<Author> authorOpt = authorRepository.findById(authorId);
                            if (authorOpt.isPresent()) {
                                book.getAuthorNames().add(authorOpt.get().getName());
                                book.getAuthorIds().add(authorId);
                            } else {
                                log.error(String.format("Author id:%s NOT FOUND!", authorId));
                                book.getAuthorNames().add("Unknown Author");
                                book.getAuthorIds().add(authorId);
                            }
                        }
                    }
                    if(bookJson.path("covers").isArray()) { 
                        for(JsonNode authorJson : bookJson.get("covers")) {
                            book.getCoverIds().add(authorJson.asText());
                        }
                    }
                    book.setDescription(bookJson.path("description").path("value").asText("NA"));
                    
                    book = bookRepository.save(book);
                    log.debug(String.format("Book:\"%s\" author:%s -> saved id:%s",
                                            book.getName(),book.getAuthorNames(), book.getId()));
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
        bootstrapBooks();
        shutdownManager.initiateShutdown(0);
    }

}
