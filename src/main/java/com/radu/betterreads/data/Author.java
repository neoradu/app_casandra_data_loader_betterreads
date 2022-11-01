package com.radu.betterreads.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("author_by_id")
public class Author {
    
    @Id @PrimaryKeyColumn(name="author_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String id;
    
    @Column(value = "name")
    @CassandraType(type= Name.TEXT)
    private String name;

    @Column(value = "personal_name")
    @CassandraType(type= Name.TEXT)
    private String personalName;
    
    public Author() {}
    
    public Author(String id, String name) {
        this.id = id;
        this.name = name;
        this.personalName = null;
    }
    
    public Author(String id, String name, String personalName) {
        super();
        this.id = id;
        this.name = name;
        this.personalName = personalName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonalName() {
        return personalName;
    }

    public void setPersonalName(String personalName) {
        this.personalName = personalName;
    }
}
