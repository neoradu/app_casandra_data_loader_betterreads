package com.radu.betterreads.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.radu.betterreads.data.Author;

@Repository
public interface AuthorRepository extends CassandraRepository<Author, String> {

}
