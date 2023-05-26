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

import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRules;
import io.helidon.nima.webserver.http.HttpService;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

import com.mongodb.client.MongoClient;
import jakarta.json.Json;

class ExitService implements HttpService {

    private static final System.Logger LOGGER = System.getLogger(ExitService.class.getName());

    private final Ora23cConnection connection;
    private final MongoClient mongoClient;
    private WebServer server;

    ExitService(Ora23cConnection connection, MongoClient mongoClient) {
            this.connection = connection;
            this.mongoClient = mongoClient;
            this.server = null;
        }

    @Override
    public void routing(HttpRules rules) {
        rules.get("/", this::exit);
    }

    void server(WebServer server) {
        if (this.server != null) {
            throw new IllegalStateException("Web server was already set");
        }
        this.server = server;
    }

    private void exit(ServerRequest request, ServerResponse response) throws Exception {
        if (connection != null) {
            connection.close();
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
        response.send(
                Json.createObjectBuilder()
                        .add("time", ExitThread.DELAY_SECONDS)
                        .build()
        );
        ExitThread.execute(server);
    }

    private static final class ExitThread implements Runnable {

        private static final int DELAY_SECONDS = 1;

        private final WebServer server;

        private static void execute(WebServer server) {
            new Thread(new ExitThread(server)).start();
        }

        private ExitThread(WebServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            System.out.println(String.format("Shutting down in %d second(s)", DELAY_SECONDS));
            try {
                // Give server few seconds to send last response
                Thread.sleep(DELAY_SECONDS * 1000);
                server.stop();
            } catch (InterruptedException e) {
                LOGGER.log(System.Logger.Level.ERROR, "Exit thread was interrupted");
            }
        }

    }

}
