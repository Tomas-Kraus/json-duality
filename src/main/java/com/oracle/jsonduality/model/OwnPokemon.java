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

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

//@IdClass(OwnPokemon.PK.class)
@Entity
@Table(name = "OwnPokemon")
public class OwnPokemon {

    public static final class PK {

        private Trainer trainer;

        private Pokemon pokemon;

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

    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable=false)
    private Trainer trainer;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable=false)
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
