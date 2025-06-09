package me.dewrs.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnection extends ConnectionFactory{
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    private HikariDataSource hikari;

    public MySQLConnection(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    @Override
    public void connect(){
        if(existPool()) return;
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        hikariConfig.addDataSourceProperty("serverName", host);
        hikariConfig.addDataSourceProperty("port", port);
        hikariConfig.addDataSourceProperty("databaseName", database);
        hikariConfig.addDataSourceProperty("user", user);
        hikariConfig.addDataSourceProperty("password", password);

        hikari = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close(){
        if(existPool()){
            hikari.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if(!existPool()){
            connect();
        }
        return hikari.getConnection();
    }

    private boolean existPool(){
        return hikari != null && !hikari.isClosed();
    }
}