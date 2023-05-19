/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.oracle.jsonduality.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@NamedQueries({
        @NamedQuery(name = "ListAllTrainers", query = "SELECT t FROM Trainer t"),
        @NamedQuery(name = "ListAllTrainersWithPokemons",
                    query = "SELECT o.trainer.name AS trainer, o.hp, o.pokemon.name AS name, o.pokemon.type.name AS typeName FROM Trainer t "
                            + "JOIN FETCH t.pokemons o JOIN FETCH o.pokemon p")
})
@Entity
@Table(name = "Trainer")
public class Trainer {

    @Id
    private int id;

    private String name;

    @OneToMany
    @JoinColumn(name = "trainer_id")
    private Collection<OwnPokemon> pokemons;

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
