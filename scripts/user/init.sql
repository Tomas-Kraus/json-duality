-- Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
--
-- This program and the accompanying materials are made available under the
-- terms of the Eclipse Public License v. 2.0, which is available at
-- http://www.eclipse.org/legal/epl-2.0.
--
-- This Source Code may also be made available under the following Secondary
-- Licenses when the conditions for such availability set forth in the
-- Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
-- version 2 with the GNU Classpath Exception, which is available at
-- https://www.gnu.org/software/classpath/license.html.
--
-- SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

-- User level initialization

-- Database relational schema
CREATE TABLE Type (
    id NUMBER NOT NULL PRIMARY KEY,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE Pokemon (
    id NUMBER NOT NULL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    type NUMBER NOT NULL REFERENCES Type (id)
);

CREATE TABLE Trainer (
    id NUMBER NOT NULL PRIMARY KEY,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE OwnPokemon (
    trainer_id NUMBER NOT NULL references Trainer (id),
    pokemon_id NUMBER NOT NULL references Pokemon (id),
    hp NUMBER NOT NULL,
    CONSTRAINT own_pokemon_pk PRIMARY KEY (trainer_id, pokemon_id)
);

-- Initial data
INSERT INTO Type (id, name) VALUES ( 1, 'Normal');
INSERT INTO Type (id, name) VALUES ( 2, 'Fighting');
INSERT INTO Type (id, name) VALUES ( 3, 'Flying');
INSERT INTO Type (id, name) VALUES ( 4, 'Poison');
INSERT INTO Type (id, name) VALUES ( 5, 'Ground');
INSERT INTO Type (id, name) VALUES ( 6, 'Rock');
INSERT INTO Type (id, name) VALUES ( 7, 'Bug');
INSERT INTO Type (id, name) VALUES ( 8, 'Ghost');
INSERT INTO Type (id, name) VALUES ( 9, 'Steel');
INSERT INTO Type (id, name) VALUES (10, 'Fire');
INSERT INTO Type (id, name) VALUES (11, 'Water');
INSERT INTO Type (id, name) VALUES (12, 'Grass');
INSERT INTO Type (id, name) VALUES (13, 'Electric');
INSERT INTO Type (id, name) VALUES (14, 'Psychic');
INSERT INTO Type (id, name) VALUES (15, 'Ice');
INSERT INTO Type (id, name) VALUES (16, 'Dragon');
INSERT INTO Type (id, name) VALUES (17, 'Dark');
INSERT INTO Type (id, name) VALUES (18, 'Fairy');

INSERT INTO Pokemon (id, name, type) VALUES ( 1, 'Pikachu', 13);
INSERT INTO Pokemon (id, name, type) VALUES ( 2, 'Bulbasaur', 12);
INSERT INTO Pokemon (id, name, type) VALUES ( 3, 'Charmander', 10);
INSERT INTO Pokemon (id, name, type) VALUES ( 4, 'Squirtle', 11);
INSERT INTO Pokemon (id, name, type) VALUES ( 5, 'Chikorita', 12);
INSERT INTO Pokemon (id, name, type) VALUES ( 6, 'Cyndaquil', 10);
INSERT INTO Pokemon (id, name, type) VALUES ( 7, 'Totodile', 11);
INSERT INTO Pokemon (id, name, type) VALUES ( 8, 'Graveler', 6);
INSERT INTO Pokemon (id, name, type) VALUES ( 9, 'Ryhorn', 5);
INSERT INTO Pokemon (id, name, type) VALUES (10, 'Omastar', 6);
INSERT INTO Pokemon (id, name, type) VALUES (11, 'Kabutops', 6);
INSERT INTO Pokemon (id, name, type) VALUES (12, 'Onix', 6);
INSERT INTO Pokemon (id, name, type) VALUES (13, 'Shieldon', 6);
INSERT INTO Pokemon (id, name, type) VALUES (14, 'Starmie', 11);
INSERT INTO Pokemon (id, name, type) VALUES (15, 'Horsea', 11);
INSERT INTO Pokemon (id, name, type) VALUES (16, 'Psyduck', 11);
INSERT INTO Pokemon (id, name, type) VALUES (17, 'Blastoise', 11);
INSERT INTO Pokemon (id, name, type) VALUES (18, 'Poliwhirl', 11);
INSERT INTO Pokemon (id, name, type) VALUES (19, 'Togetic', 18);

INSERT INTO Trainer (id, name) VALUES (1, 'Ash');
INSERT INTO Trainer (id, name) VALUES (2, 'Brock');
INSERT INTO Trainer (id, name) VALUES (3, 'Misty');

-- Pokemons owned by Ash
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (1,  1, 325);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (1,  2, 310);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (1,  3, 286);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (1,  4, 198);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (1,  5, 314);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (1,  6, 247);

-- Pokemons owned by Brock
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (2,  8, 279);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (2,  9, 301);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (2, 10, 189);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (2, 11, 294);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (2, 12, 311);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (2, 13, 217);

-- Pokemons owned by Misty
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (3, 14, 312);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (3, 15, 321);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (3, 16, 271);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (3, 17, 182);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (3, 18, 293);
INSERT INTO OwnPokemon (trainer_id, pokemon_id, hp) VALUES (3, 19, 284);

/*
CREATE OR REPLACE JSON RELATIONAL DUALITY VIEW TrainersView AS
    SELECT JSON {
        '_id': tr.id,
        'name': tr.name,
        'pokemons': [
            SELECT JSON {
                    'pokemon_id': o.pokemon_id,
                    'trainer_id': o.trainer_id,
                    'hp': o.hp,
                    UNNEST (
                        SELECT JSON {
                            'id': p.id,
                            'name': p.name,
                            UNNEST (
                                SELECT JSON {
                                    'type_id': tp.id,
                                    'type': tp.name
                                }
                                FROM Type tp WHERE tp.id = p.type
                            )
                        }
                        FROM Pokemon p WHERE p.id = o.pokemon_id
                    )
                }
                FROM OwnPokemon o WHERE tr.id = o.trainer_id
        ]
    }
    FROM Trainer tr;
*/

-- MongoDB API-Compatible Duality View requires _id
CREATE OR REPLACE JSON RELATIONAL DUALITY VIEW TrainersView AS
    Trainer {
        _id: id
        name: name
        pokemons: OwnPokemon [ {
            pokemon_id: pokemon_id
            trainer_id: trainer_id
            hp: hp
            Pokemon @unnest {
                id: id
                name: name
                Type @unnest {
                    type_id: id
                    type: name
                }
            }
        } ]
    };

-- MongoDB API-Compatible Duality View requires _id
CREATE OR REPLACE JSON RELATIONAL DUALITY VIEW Pokemons AS
    Pokemon @insert @update @delete {
       _id: id
       name: name
       Type @unnest @noinsert @noupdate @nodelete {
           type_id: id
           type: name
       }
    };

/*
CREATE OR REPLACE JSON RELATIONAL DUALITY VIEW Pokemons AS
    SELECT JSON {
        '_id': p.id,
        'name': p.name,
        UNNEST (
            SELECT JSON {
                'type_id': t.id,
                'type': t.name
            }
            FROM Type t WITH NOINSERT NOUPDATE NODELETE WHERE t.id = p.type
        )
    }
    FROM Pokemon p WITH INSERT UPDATE DELETE;
*/

-- List user tables: SELECT * FROM show_tables;
CREATE VIEW show_tables AS
    SELECT table_name
           FROM user_tables
           ORDER BY table_name;
