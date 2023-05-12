package com.oracle.jsonduality;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class Ora23cConnection implements AutoCloseable {

    private final Connection connection;

    Ora23cConnection(String url) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Connection get() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

}
