package me.dewrs.storage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnection extends ConnectionFactory{
    private File dataFolder;

    public SQLiteConnection(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    public void connect(){
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            File dbFile = new File(dataFolder, "storage.db");
            if (dbFile.exists()) {
                dbFile.createNewFile();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            setupSQLite(connection);
        }catch (SQLException | IOException ex){
            ex.printStackTrace();
        }
    }

    private void setupSQLite(Connection connection){
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close(){
        if(connection == null) return;
        try{
            connection.close();
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }
}