package com.oracle.jsonduality.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OwnPokemon {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable=false)
    private Trainer trainer;

    @Id
    @ManyToOne
    @JoinColumn(name = "pokemon_id")
    private Pokemon pokemon;

    private int hp;

    public OwnPokemon() {
        this(null, null, -1);
    }

    public OwnPokemon(Trainer trainer, Pokemon pokemon, int hp) {
        this.trainer = trainer;
        this.pokemon = pokemon;
        this.hp = hp;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

}
