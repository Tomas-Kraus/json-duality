package com.oracle.jsonduality.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;

@NamedQueries(
        @NamedQuery(name = "ListAllTrainers", query = "SELECT t FROM Trainer t")
)
@Entity
public class Trainer {

    @Id
    private int id;

    private String name;

    @OneToMany
    private Set<OwnPokemon> pokemons;

    public Trainer() {
        this(-1, null, Collections.EMPTY_SET);
    }

    public Trainer(int id, String name, Set<OwnPokemon> pokemons) {
        this.id = id;
        this.name = name;
        this.pokemons = pokemons;
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
