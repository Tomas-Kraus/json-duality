package com.oracle.jsonduality.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

@NamedQueries(
        @NamedQuery(name = "ListAllTypes", query = "SELECT t FROM Type t")
)
@Entity
public class Type {

    @Id
    private int id;

    private String name;

    public Type() {
        this(-1, null);
    }

    public Type(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
