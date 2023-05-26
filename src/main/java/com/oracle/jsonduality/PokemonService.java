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

package com.oracle.jsonduality;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.helidon.common.http.Http;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.nima.webserver.http.HttpRules;
import io.helidon.nima.webserver.http.HttpService;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

class PokemonService implements HttpService {

   private final Ora23cConnection connection;

    PokemonService(Ora23cConnection connection) {
        this.connection = connection;
    }

    @Override
    public void routing(HttpRules rules) {
        rules
                .get("/", this::index)
                .get("/trainers", this::listTrainers)
                .post("/insert", (req, res) -> insertPokemons(req.content().as(JsonStructure.class), res))
                .post("/delete", (req, res) -> deletePokemons(req.content().as(JsonArray.class), res))
                .post("/update", (req, res) -> updatePokemons(req.content().as(JsonStructure.class), res))
                .get("/list", this::listPokemons);
    }

    /**
     * Return index page.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void index(ServerRequest request, ServerResponse response) {
        response.headers().contentType(MediaTypes.TEXT_PLAIN);
        response.send("Pokemon JSON-Duality Example:\n"
                              + "     GET  /pokemon/trainers - List all traineirs and their pokemons\n"
                              + "     GET  /pokemon/list     - List all pokemons\n"
                              + "     POST /pokemon/insert   - Insert pokemon(s)\n"
                              + "     POST /pokemon/delete   - Delete pokemon(s)\n"
                              + "     POST /pokemon/update   - Update pokemon(s)\n"
        );
    }

    // wget -O- http://localhost:8080/pokemon/list
    // curl http://localhost:8080/pokemon/list
    /**
     * Return all stored traineirs and their pokemons.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void listTrainers(ServerRequest request, ServerResponse response) {
        try (Statement stmt = connection.get().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM TrainersView");
            JsonArrayBuilder builder = Json.createArrayBuilder();
            while (rs.next()) {
                builder.add(rs.getObject(1, JsonValue.class));
            }
            response.send(builder.build());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // wget -O- http://localhost:8080/pokemon/list
    // curl --header='Content-Type:application/json' http://localhost:8080/pokemon/list
    /**
     * Return all stored pokemons.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void listPokemons(ServerRequest request, ServerResponse response) {
        try (Statement stmt = connection.get().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Pokemons");
            JsonArrayBuilder builder = Json.createArrayBuilder();
            while (rs.next()) {
                builder.add(rs.getObject(1, JsonValue.class));
            }
            response.send(builder.build());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Insert new pokemon(s) from JSON POST request.
    // wget -O- --post-data='{"id":20,"name":"Gloom","type_id":12}' --header='Content-Type:application/json' http://localhost:8080/pokemon/insert
    // curl -X POST -H "Content-Type: application/json" -d '{"_id":20,"name":"Gloom","type_id":12}' http://localhost:8080/pokemon/insert
    /**
     * Insert new pokemon(s).
     *
     * @param pokemons the pokemon(s) to insert (JsonObject or JsonArray of JsonObjects)
     * @param response the server response
     */
    private void insertPokemons(JsonStructure pokemons, ServerResponse response) {
        try (PreparedStatement stmt = connection.get().prepareStatement("INSERT INTO Pokemons VALUES(?)")) {
            int count = 0;
            switch (pokemons.getValueType()) {
                case ARRAY:
                    for (JsonValue pokemon : pokemons.asJsonArray()) {
                        if (pokemon.getValueType() != JsonValue.ValueType.OBJECT) {
                            throw new RuntimeException("Pokemon value is not JsonObject");
                        }
                        stmt.setObject(1, pokemon.asJsonObject());
                        count += stmt.executeUpdate();
                    }
                    break;
                case OBJECT:
                    stmt.setObject(1, pokemons.asJsonObject());
                    count += stmt.executeUpdate();
                    break;
                default:
                    throw new RuntimeException("Expected JsonArray or JsonObject");
            }
            response.send(
                    Json.createObjectBuilder()
                            .add("count", count)
                            .build()
            );
        } catch (SQLException e) {
            response.status(Http.Status.INTERNAL_SERVER_ERROR_500)
                    .send(
                    Json.createObjectBuilder()
                            .add("exception", e.getClass().getName())
                            .add("message", e.getMessage())
                            .build()
            );
        }
    }

    // Delete pokemon(s) from JSON POST request.
    // wget -O- --post-data='[20]' --header='Content-Type:application/json' http://localhost:8080/pokemon/delete
    // curl -X POST -H "Content-Type: application/json" -d '[20]' http://localhost:8080/pokemon/delete
    private void deletePokemons(JsonArray ids, ServerResponse response) {
        try (PreparedStatement stmt = connection.get().prepareStatement("DELETE FROM Pokemons p WHERE p.data._id = ?")) {
            int count = 0;
            if (ids.getValueType() != JsonValue.ValueType.ARRAY) {
                throw new RuntimeException("Pokemon value is not JsonArray");
            }
            for (JsonValue id : ids) {
                if (id.getValueType() != JsonValue.ValueType.NUMBER) {
                    throw new RuntimeException("Pokemon ID value is not JsonNumber");
                }
                stmt.setObject(1, id);
                count += stmt.executeUpdate();
            }
            response.send(
                    Json.createObjectBuilder()
                            .add("count", count)
                            .build()
            );
        } catch (SQLException e) {
            response.status(Http.Status.INTERNAL_SERVER_ERROR_500)
                    .send(
                            Json.createObjectBuilder()
                                    .add("exception", e.getClass().getName())
                                    .add("message", e.getMessage())
                                    .build()
                    );
        }
    }


    // Update pokemon(s) from JSON POST request.
    // curl -X POST -H "Content-Type: application/json" -d '{"_id":20,"name":"Vileplume","type_id":4}' http://localhost:8080/pokemon/update
    private void updatePokemons(JsonStructure pokemons, ServerResponse response) {
        try (PreparedStatement stmt = connection.get().prepareStatement("UPDATE Pokemons SET data = ? WHERE json_value(data, '$._id.numberOnly()') = ?")) {
            int count = 0;
            switch (pokemons.getValueType()) {
            case ARRAY:
                for (JsonValue pokemon : pokemons.asJsonArray()) {
                    if (pokemon.getValueType() != JsonValue.ValueType.OBJECT) {
                        throw new RuntimeException("Pokemon value is not JsonObject");
                    }
                    stmt.setObject(1, pokemon.asJsonObject());
                    stmt.setObject(2, pokemon.asJsonObject().get("_id"));
                    count += stmt.executeUpdate();
                }
                break;
            case OBJECT:
                stmt.setObject(1, pokemons.asJsonObject());
                stmt.setObject(2, pokemons.asJsonObject().get("_id"));
                count += stmt.executeUpdate();
                break;
            default:
                throw new RuntimeException("Expected JsonArray or JsonObject");
            }
            response.send(
                    Json.createObjectBuilder()
                            .add("count", count)
                            .build()
            );
        } catch (SQLException e) {
            response.status(Http.Status.INTERNAL_SERVER_ERROR_500)
                    .send(
                            Json.createObjectBuilder()
                                    .add("exception", e.getClass().getName())
                                    .add("message", e.getMessage())
                                    .build()
                    );
        }
    }

}
