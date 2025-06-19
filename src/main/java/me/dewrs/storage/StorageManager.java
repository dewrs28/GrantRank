package me.dewrs.storage;

import me.dewrs.GrantRank;
import me.dewrs.config.ConfigManager;
import me.dewrs.logger.LogMessage;
import me.dewrs.logger.LogSender;
import me.dewrs.model.NodeLog;
import net.luckperms.api.context.Context;
import net.luckperms.api.context.MutableContextSet;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

public class StorageManager {
    private GrantRank plugin;
    private StorageType storageType;

    public StorageManager(GrantRank plugin){
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        ConfigManager configManager = plugin.getConfigManager();
        StorageType storageType = plugin.getConfigManager().getStorageType();
        ConnectionFactory connectionFactory;
        switch (storageType) {
            case MYSQL: {
                String host = configManager.getStorageHost();
                int port = configManager.getStoragePort();
                String database = configManager.getStorageDatabase();
                String user = configManager.getStorageUser();
                String password = configManager.getStoragePassword();
                connectionFactory = new MySQLConnection(host, port, database, user, password);
                break;
            }
            case SQLITE: {
                connectionFactory = new SQLiteConnection(plugin.getDataFolder());
                break;
            }
            default:
                connectionFactory = null;
                break;
        }
        plugin.setConnectionFactory(connectionFactory);
        try (Connection con = getConnection()) {
            this.storageType = storageType;
        } catch (SQLException e) {
            LogSender.sendLogMessage(LogMessage.STORAGE_CONNECTION_ERROR.format(storageType.toString()));
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        createTables();
        migrateDatabase(() -> {
            LogSender.sendLogMessage(LogMessage.STORAGE_CORRECT.format(storageType.toString()));
        });
    }

    public Connection getConnection() throws SQLException {
        return plugin.getConnectionFactory().getConnection();
    }

    private void createTables(){
        try (Connection con = getConnection()) {
            String incrementProperty = "AUTOINCREMENT";
            if(storageType.equals(StorageType.MYSQL)) incrementProperty = "AUTO_INCREMENT";
            PreparedStatement statementMain = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS nodes_logs ("+
                    "id INTEGER PRIMARY KEY "+incrementProperty+","+
                    "uuid_user TEXT NOT NULL,"+
                    "name_user TEXT NOT NULL,"+
                    "name_operator TEXT NOT NULL,"+
                    "node TEXT NOT NULL,"+
                    "expiry BIGINT NOT NULL,"+
                    "reason TEXT NOT NULL,"+
                    "creation_time BIGINT NOT NULL," +
                    "revoked BOOLEAN NOT NULL)");
            statementMain.executeUpdate();
            PreparedStatement statementContexts = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS contexts_logs (" +
                            "id INTEGER PRIMARY KEY "+incrementProperty+","+
                            "node_id INTEGER NOT NULL,"+
                            "context_key TEXT NOT NULL,"+
                            "context_value TEXT NOT NULL,"+
                            "FOREIGN KEY (node_id) REFERENCES nodes_logs(id) ON DELETE CASCADE)");
            statementContexts.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void createNodeLog(NodeLog nodeLog, Runnable callBack){
        new BukkitRunnable(){
            @Override
            public void run() {
                try (Connection con = getConnection()) {
                    PreparedStatement statement = con.prepareStatement("INSERT INTO nodes_logs ("+
                                    "uuid_user, name_user, name_operator, node, expiry, reason, creation_time, revoked) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    statement.setString(1, nodeLog.getUuid_user().toString());
                    statement.setString(2, nodeLog.getName_user());
                    statement.setString(3, nodeLog.getName_operator());
                    statement.setString(4, nodeLog.getNode());
                    statement.setLong(5, nodeLog.getExpiry());
                    statement.setString(6, nodeLog.getReason());
                    statement.setLong(7, nodeLog.getCreation_time());
                    statement.setBoolean(8, nodeLog.isRevoked());
                    statement.executeUpdate();

                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    int nodeId = -1;
                    if (generatedKeys.next()) {
                        nodeId = generatedKeys.getInt(1);
                    }
                    for (Context context : nodeLog.getContextSet().toSet()) {
                        try {
                            PreparedStatement statementContext = con.prepareStatement("INSERT INTO contexts_logs (" +
                                    "node_id, context_key, context_value) VALUES (?, ?, ?)");
                            statementContext.setInt(1, nodeId);
                            statementContext.setString(2, context.getKey());
                            statementContext.setString(3, context.getValue());
                            statementContext.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    Bukkit.getScheduler().runTask(plugin, callBack);
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void updateRevokeNodeLog(int id, boolean isRevoked, Runnable callBack) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = getConnection()) {
                    PreparedStatement statement = con.prepareStatement("UPDATE nodes_logs SET revoked = ? WHERE id = ?",
                            Statement.RETURN_GENERATED_KEYS);
                    statement.setBoolean(1, isRevoked);
                    statement.setInt(2, id);
                    statement.executeUpdate();
                    Bukkit.getScheduler().runTask(plugin, callBack);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    //QueryType:
    //1 : Normal - With "revoked"
    //2 : v1.1 - Without "revoked"
    public void getNodeLogs(int queryType, Consumer<TreeMap<Integer, NodeLog>> callBack){
        String query = "";
        if(queryType == 1) query = "SELECT n.id, n.uuid_user, n.name_user, n.name_operator, n.node, n.expiry, n.reason, n.creation_time, n.revoked, c.context_key, c.context_value "+
                "FROM nodes_logs n " +
                "LEFT JOIN contexts_logs c " +
                "ON n.id = c.node_id";
        if(queryType == 2) query = "SELECT n.id, n.uuid_user, n.name_user, n.name_operator, n.node, n.expiry, n.reason, n.creation_time, c.context_key, c.context_value "+
                    "FROM nodes_logs n " +
                    "LEFT JOIN contexts_logs c " +
                    "ON n.id = c.node_id";
        TreeMap<Integer, NodeLog> nodesLogs = new TreeMap<>();
        String finalQuery = query;
        new BukkitRunnable(){
            @Override
            public void run() {
                try (Connection con = getConnection()) {
                    PreparedStatement statement = con.prepareStatement(finalQuery);
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()){
                        int id = resultSet.getInt("id");
                        String contextKey = resultSet.getString("context_key");
                        String contextValue = resultSet.getString("context_value");
                        if(nodesLogs.containsKey(id)){
                            NodeLog nodeLog = nodesLogs.get(id);
                            if (contextKey != null && contextValue != null) {
                                nodeLog.getContextSet().add(contextKey, contextValue);
                            }
                            continue;
                        }
                        UUID uuid_user = UUID.fromString(resultSet.getString("uuid_user"));
                        String name_user = resultSet.getString("name_user");
                        String name_operator = resultSet.getString("name_operator");
                        String node = resultSet.getString("node");
                        long expiry = resultSet.getLong("expiry");
                        String reason = resultSet.getString("reason");
                        long creation_time = resultSet.getLong("creation_time");
                        MutableContextSet contextSet = MutableContextSet.create();
                        if (contextKey != null && contextValue != null) {
                            contextSet.add(contextKey, contextValue);
                        }
                        boolean isRevoked = false;
                        if(queryType == 1) isRevoked = resultSet.getBoolean("revoked");
                        NodeLog nodeLog = new NodeLog(uuid_user, name_user, name_operator, node, expiry, reason, contextSet, creation_time, isRevoked);
                        nodeLog.setId(id);
                        nodesLogs.put(id, nodeLog);
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> callBack.accept(nodesLogs));
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void purgeNodeLogs() {
        try (Connection con = getConnection()) {
            PreparedStatement statement1 = con.prepareStatement("DELETE FROM contexts_logs");
            PreparedStatement statement2 = con.prepareStatement("DELETE FROM nodes_logs");
            PreparedStatement statement3;
            PreparedStatement statement4;
            switch (storageType) {
                case MYSQL: {
                    statement3 = con.prepareStatement("ALTER TABLE nodes_logs AUTO_INCREMENT = 1");
                    statement4 = con.prepareStatement("ALTER TABLE contexts_logs AUTO_INCREMENT = 1");
                    break;
                }
                case SQLITE:
                default: {
                    statement3 = con.prepareStatement("DELETE FROM sqlite_sequence WHERE name='nodes_logs'");
                    statement4 = con.prepareStatement("DELETE FROM sqlite_sequence WHERE name='contexts_logs'");
                    break;
                }
            }
            statement1.executeUpdate();
            statement2.executeUpdate();
            statement3.executeUpdate();
            statement4.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteNodeLogsTables(){
        try (Connection con = getConnection()) {
            PreparedStatement statement1 = con.prepareStatement("DROP TABLE IF EXISTS contexts_logs");
            PreparedStatement statement2 = con.prepareStatement("DROP TABLE IF EXISTS nodes_logs");
            statement1.executeUpdate();
            statement2.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void migrateDatabase(Runnable callBack){
        try (Connection con = getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM nodes_logs LIMIT 1");
            ResultSet rs = statement.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                if (metaData.getColumnName(i).equalsIgnoreCase("revoked")) {
                    callBack.run();
                    return;
                }
            }

            getNodeLogs(2, nodeLogs -> {
               deleteNodeLogsTables();
               createTables();
               processSequentially(nodeLogs, 0, callBack);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processSequentially(TreeMap<Integer, NodeLog> entries, int index, Runnable callBack) {
        List<Map.Entry<Integer, NodeLog>> list = new ArrayList<>(entries.entrySet());
        if (index >= list.size()) {
            callBack.run();
            return;
        }

        NodeLog nodeLog = list.get(index).getValue();
        createNodeLog(nodeLog, () -> processSequentially(entries, index + 1, callBack));
    }

    public StorageType getStorageType() {
        return storageType;
    }
}
