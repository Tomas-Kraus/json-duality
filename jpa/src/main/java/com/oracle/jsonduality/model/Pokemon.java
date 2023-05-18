package com.oracle.jsonduality.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

@NamedQueries(
        @NamedQuery(name = "ListAllPokemons", query = "SELECT p FROM Pokemon p")
)
@Entity
public class Pokemon {

    @Id
    private int id;

    private String name;

    @Column(name = "type")
    private int typeId;

    @ManyToOne
    // JPARS thrown an exception when @ManyToOne relation is used for insert.
    @JoinColumn(name="type", insertable = false, updatable = false)
    private Type type;

    public Pokemon() {
        this(-1, null, null);
    }

    public Pokemon(int id, String name, Type type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
