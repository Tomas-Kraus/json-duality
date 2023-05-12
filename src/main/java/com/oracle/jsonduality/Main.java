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

import java.lang.System.Logger;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class Main {

    private static final Logger LOGGER = System.getLogger(Main.class.getName());

    private static final JsonWriterFactory WRITER_FACTORY = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
    public static void main(String[] args) {
        if (args.length < 1) {
            LOGGER.log(Logger.Level.ERROR, "Missing Oracle 23c URL");
            System.out.println("Usage: java -jar <jar_pkg> <oradb_url>");
            return;
        }
        try (Ora23cConnection connection = new Ora23cConnection(args[0])) {
            LOGGER.log(Logger.Level.INFO, String.format("Connected to Oracle 23c URL: %s", args[0]));
            Pokemons pokemons = new Pokemons(connection);
            JsonValue trainers = pokemons.listTrainers();
            if (trainers != null) {
                printJson(trainers);
            } else {
                LOGGER.log(System.Logger.Level.INFO, "No trainers were returned");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void printJson(JsonValue value) {
        JsonWriter jsonWriter = WRITER_FACTORY.createWriter(System.out);
        jsonWriter.write(value);
        System.out.println();
    }
}
