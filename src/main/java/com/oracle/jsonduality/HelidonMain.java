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

import io.helidon.config.Config;
import io.helidon.config.ConfigValue;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;

public class HelidonMain {

    private static final System.Logger LOGGER = System.getLogger(HelidonMain.class.getName());

    public static void main(String[] args) {
        Config config = Config.create();
        ConfigValue<String> configUrl = config.get("db.url").as(String.class);
        if (configUrl.isPresent()) {
            Ora23cConnection connection = new Ora23cConnection(configUrl.get());
            startServer(config, connection);
        } else {
            LOGGER.log(System.Logger.Level.ERROR, "Missing Oracle 23c URL");
        }
    }

    /**
     * Start the server.
     *
     * @return the created {@link io.helidon.nima.webserver.WebServer} instance
     */
    static WebServer startServer(Config config, Ora23cConnection connection) {

        ExitService exitService = new ExitService(connection);


        WebServer server = WebServer.builder()
                .config(config)
                .routing(routing -> routing(config, routing, exitService))
                .build();

        exitService.server(server);

        server.start();
        System.out.println();
        System.out.println("WEB server is up!");
        System.out.println(String.format("  GET  http://localhost:%d/pokemon/list - list all pokemons", server.port()));
        System.out.println(String.format("  POST http://localhost:%d/pokemon/insert - insert pokemon(s)", server.port()));
        System.out.println(String.format("  POST http://localhost:%d/pokemon/delete - delete pokemon(s)", server.port()));
        System.out.println(String.format("  POST http://localhost:%d/pokemon/update - update pokemon(s)", server.port()));
        System.out.println(String.format("  GET  http://localhost:%d/exit - shut down the server", server.port()));

        return server;
    }

    /**
     * Updates HTTP Routing.
     *
     * @param routing routing builder
     */
    private static void routing(Config config, HttpRouting.Builder routing, ExitService exitService) {
        ConfigValue<String> configUrl = config.get("db.url").as(String.class);
        if (configUrl.isPresent()) {
            Ora23cConnection connection = new Ora23cConnection(configUrl.get());
            routing.register("/pokemon", new PokemonService(connection))
                    .register("/exit", exitService)
                    .build();
        } else {
            LOGGER.log(System.Logger.Level.ERROR, "Missing Oracle 23c URL");
        }
    }

}
