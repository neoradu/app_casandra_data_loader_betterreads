package com.radu.betterreads.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.radu.betterreads.data.Book;

@Repository
public interface BookRepository extends CassandraRepository<Book, String> {

}
