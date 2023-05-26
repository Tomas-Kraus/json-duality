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

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.helidon.config.Config;
import io.helidon.config.ConfigValue;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class HelidonMain {

    private static final System.Logger LOGGER = System.getLogger(HelidonMain.class.getName());

    public static void main(String[] args) throws Exception {
        Config config = Config.create();
        ConfigValue<String> configUrl = config.get("db.url").as(String.class);
        ConfigValue<String> configMongoUrl = config.get("db.mongo.url").as(String.class);
        Ora23cConnection connection;
        if (configUrl.isPresent()) {
            connection = new Ora23cConnection(configUrl.get());
        } else {
            connection = null;
            LOGGER.log(System.Logger.Level.ERROR, "Missing Oracle 23c URL");
        }
        MongoClient mongoClient;
        if (configMongoUrl.isPresent()) {
            String url = configMongoUrl.get();
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }

            } };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(url))
                            .applyToSslSettings(builder -> builder
                                    .context(sc)
                                    .enabled(true))
                            //.applyToSocketSettings(builder -> builder.keepAlive(false))
                            .build());
            LOGGER.log(System.Logger.Level.INFO, String.format("connecting to Oracle 23c Mongo API URL %s", url));

            //mongoClient = MongoClients.create(url);
        } else {
            mongoClient = null;
            LOGGER.log(System.Logger.Level.ERROR, "Missing Oracle 23c Mongo API URL");
        }
        if (connection != null && mongoClient != null) {
            startServer(config, connection, mongoClient);
        }
    }

    /**
     * Start the server.
     *
     * @return the created {@link io.helidon.nima.webserver.WebServer} instance
     */
    static WebServer startServer(Config config, Ora23cConnection connection, MongoClient mongoClient) {

        ExitService exitService = new ExitService(connection, mongoClient);


        WebServer server = WebServer.builder()
                .config(config)
                .routing(routing -> routing(config, routing, exitService, connection, mongoClient))
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
    private static void routing(Config config, HttpRouting.Builder routing, ExitService exitService, Ora23cConnection connection, MongoClient mongoClient) {
        routing.register("/exit", exitService)
                .register("/pokemon", new PokemonService(connection))
                .register("/mongo", new MongoService(mongoClient, config.get("db.mongo.database").as(String.class)))
                .build();
    }

}
