package me.dewrs.storage;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ConnectionFactory {
    protected Connection connection;

    protected abstract void connect();

    protected abstract void close();

    protected Connection getConnection() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            return connection;
        }
        connect();
        return connection;
    }

    public void closeConnection(){
        close();
    }
}