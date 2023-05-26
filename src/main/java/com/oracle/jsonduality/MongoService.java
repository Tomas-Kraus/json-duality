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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import io.helidon.config.ConfigValue;
import io.helidon.nima.webserver.http.HttpRules;
import io.helidon.nima.webserver.http.HttpService;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonNumber;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;

public class MongoService implements HttpService {

    private static final System.Logger LOGGER = System.getLogger(MongoService.class.getName());

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDb;

    MongoService(MongoClient mongoClient, ConfigValue<String> configDatabase) {
        this.mongoClient = mongoClient;
        if (configDatabase.isPresent()) {
            this.mongoDb = mongoClient.getDatabase(configDatabase.get());
        } else{
            this.mongoDb = null;
            LOGGER.log(System.Logger.Level.ERROR, "Missing Oracle Mongo API compatible database name");
        }
    }

    @Override
    public void routing(HttpRules rules) {
        rules
                .get("/list", this::listPokemons)
                .get("/listView", this::listPokemonsView)
                .get("/get/{id}", this::getPokemon)
                .post("/insert", (req, res) -> insertPokemons(req.content().as(JsonStructure.class), res))
                .post("/update", (req, res) -> updatePokemons(req.content().as(JsonStructure.class), res))
                .post("/delete", (req, res) -> deletePokemons(req.content().as(JsonArray.class), res));

    }

    // curl http://localhost:8080/mongo/listView
    /**
     * Return all stored pokemons.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void listPokemonsView(ServerRequest request, ServerResponse response) {
        MongoCollection<Document> collection = mongoDb.getCollection("pokemon_mongo");
        JsonArrayBuilder jab = Json.createArrayBuilder();
        FindIterable<Document> result = collection.find();
        result.forEach(
                (Consumer<? super Document>) item -> {
                    String jsonString = item.toJson();
                    JsonReader reader = Json.createReader(new StringReader(jsonString));
                    jab.add(reader.readValue());
                    reader.close();
                }
        );
        response.send(jab.build());
    }

    // curl http://localhost:8080/mongo/list
    /**
     * Return all stored pokemons.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void listPokemons(ServerRequest request, ServerResponse response) {
        MongoCollection<Document> collection = mongoDb.getCollection("pokemon_documents");
        JsonArrayBuilder jab = Json.createArrayBuilder();
        FindIterable<Document> result = collection.find();
        result.forEach(
                (Consumer<? super Document>) item -> {
                    String jsonString = item.toJson();
                    JsonReader reader = Json.createReader(new StringReader(jsonString));
                    jab.add(reader.readValue());
                    reader.close();
                }
        );
        response.send(jab.build());
    }

    // curl http://localhost:8080/mongo/get/<id>
    /**
     * Get pokemon by ID.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void getPokemon(ServerRequest request, ServerResponse response) {
        int pokemonId = Integer.parseInt(request.path().pathParameters().value("id"));
        MongoCollection<Document> collection = mongoDb.getCollection("pokemon_documents");
        Bson query = Filters.eq("_id", pokemonId);
        FindIterable<Document> result = collection.find(query);
        Document pokemon = result.first();
        if (pokemon == null) {
            response.send(JsonValue.NULL.toString());
        } else {
            JsonReader reader = Json.createReader(new StringReader(pokemon.toJson()));
            JsonValue jsonValue = reader.readValue();
            reader.close();
            response.send(jsonValue);
        }
    }

    // curl -X POST -H "Content-Type: application/json" -d '{"_id":20,"name":"Gloom","type_id":12}' http://localhost:8080/mongo/insert
    /**
     * Insert new pokemon(s).
     *
     * @param pokemons the pokemon(s) to insert (JsonObject or JsonArray of JsonObjects)
     * @param response the server response
     */
    private void insertPokemons(JsonStructure pokemons, ServerResponse response) {
        MongoCollection<Document> collection = mongoDb.getCollection("pokemon_documents");
        int count = 0;
        switch (pokemons.getValueType()) {
        case ARRAY:
            for (JsonValue pokemon : pokemons.asJsonArray()) {
                if (pokemon.getValueType() != JsonValue.ValueType.OBJECT) {
                    throw new RuntimeException("Pokemon value is not JsonObject");
                }
                collection.insertOne(new Document(jsonToBsonObject(pokemon.asJsonObject())));
                count++;
            }
            break;
        case OBJECT:
            collection.insertOne(new Document(jsonToBsonObject(pokemons.asJsonObject())));
            count++;
            break;
        default:
            throw new RuntimeException("Expected JsonArray or JsonObject");
        }
        response.send(
                Json.createObjectBuilder()
                        .add("count", count)
                        .build()
        );
    }

    // Delete pokemon(s) from JSON POST request.
    // curl -X POST -H "Content-Type: application/json" -d '[20]' http://localhost:8080/mongo/delete
    private void deletePokemons(JsonArray ids, ServerResponse response) {
        MongoCollection<Document> collection = mongoDb.getCollection("pokemon_documents");
        int count = 0;
        if (ids.getValueType() != JsonValue.ValueType.ARRAY) {
            throw new RuntimeException("Pokemon value is not JsonArray");
        }
        for (JsonValue id : ids) {
            if (id.getValueType() != JsonValue.ValueType.NUMBER) {
                throw new RuntimeException("Pokemon ID is not JsonNumber");
            }
            Bson query = Filters.eq("_id", ((JsonNumber) id).intValue());
            DeleteResult result = collection.deleteOne(query);
            count += result.getDeletedCount();
        }
        response.send(
                Json.createObjectBuilder()
                        .add("count", count)
                        .build()
        );
    }

    // curl -X POST -H "Content-Type: application/json" -d '{"_id":20,"name":"Vileplume","type_id":4}' http://localhost:8080/mongo/update
    private void updatePokemons(JsonStructure pokemons, ServerResponse response) {
        MongoCollection<Document> collection = mongoDb.getCollection("pokemon_documents");
        int count = 0;
        switch (pokemons.getValueType()) {
        case ARRAY:
            for (JsonValue pokemon : pokemons.asJsonArray()) {
                count += updatePokemon(collection, pokemon);
            }
            break;
        case OBJECT:
            count += updatePokemon(collection, pokemons.asJsonObject());
            break;
        default:
            throw new RuntimeException("Expected JsonArray or JsonObject");
        }
        response.send(
                Json.createObjectBuilder()
                        .add("count", count)
                        .build()
        );
    }

    private static long updatePokemon(MongoCollection<Document> collection, JsonValue pokemon) {
        if (pokemon.getValueType() != JsonValue.ValueType.OBJECT) {
            throw new RuntimeException("Pokemon value is not JsonObject");
        }
        JsonValue id = pokemon.asJsonObject().get("_id");
        if (id == null) {
            throw new RuntimeException("Pokemon _id value is missing");
        }
        if (id.getValueType() != JsonValue.ValueType.NUMBER) {
            throw new RuntimeException("Pokemon ID is not JsonNumber");
        }
        Bson query = Filters.eq("_id", jsonToBsonNumber((JsonNumber) id));
        BsonDocument data = jsonToBsonObject(pokemon.asJsonObject());
        List<Bson> updateItems = new ArrayList<>(data.size());
        data.forEach((key, value) -> {
            if (!"_id".equals(key)) {
                updateItems.add(Updates.set(key, value));
            }
        });
        UpdateResult result = collection.updateOne(query, Updates.combine(updateItems));
        return result.getModifiedCount();
    }

    // Jakarta JSON to BSON converter

    private static BsonValue jsonToBson(JsonValue value) {
        if (value == null) {
            return null;
        }
        switch (value.getValueType()) {
            case NULL: return BsonNull.VALUE;
            case TRUE: return BsonBoolean.TRUE;
            case FALSE: return BsonBoolean.FALSE;
            case NUMBER: return jsonToBsonNumber((JsonNumber) value);
            case STRING: return new BsonString(((JsonString) value).getString());
            case ARRAY: return jsonToBsonArray((JsonArray) value);
            case OBJECT: return jsonToBsonObject((JsonObject) value);
        }
        throw new IllegalArgumentException(String.format("JsonValue type %s is not supported", value.getValueType().name()));
    }

    private static BsonNumber jsonToBsonNumber(JsonNumber value) {
        Number numberValue = value.numberValue();
        if ((numberValue instanceof Byte) || (numberValue instanceof Short)
                || (numberValue instanceof Integer) || (numberValue instanceof AtomicInteger)) {
            return new BsonInt32(numberValue.intValue());
        }
        if ((numberValue instanceof Long) || (numberValue instanceof AtomicLong)
                || (numberValue instanceof LongAccumulator) || (numberValue instanceof LongAdder)) {
            return new BsonInt64(numberValue.longValue());
        }
        if ((numberValue instanceof Float) || (numberValue instanceof Double)
                || (numberValue instanceof DoubleAccumulator) || (numberValue instanceof DoubleAdder)) {
            return new BsonDouble(numberValue.doubleValue());
        }
        return new BsonDecimal128(new Decimal128(value.bigDecimalValue()));
    }

    private static BsonArray jsonToBsonArray(JsonArray value) {
        BsonArray array = new BsonArray();
        value.forEach(jsonValue -> array.add(jsonToBson(jsonValue)));
        return array;
    }

    private static BsonDocument jsonToBsonObject(JsonObject value) {
        BsonDocument object = new BsonDocument();
        value.forEach((jsonKey, jsonValue) -> object.append(jsonKey, jsonToBson(jsonValue)));
        return object;
    }

}
