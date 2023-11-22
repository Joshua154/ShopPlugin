package de.joshua.util.database;

import de.joshua.ShopPlugin;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class SQLiteQueue {
    private static final String DATABASE_URL = "jdbc:sqlite:/path/to/your/database.db";
    private static List<String> DATABASE_TABLES = null;
    ShopPlugin shopPlugin;
    private final Queue<Pair<String, CompletableFuture<ResultSet>>> operationQueue;
    private Connection connection;

    public SQLiteQueue(ShopPlugin shopPlugin) {
        operationQueue = new LinkedList<>();
        this.shopPlugin = shopPlugin;
        DATABASE_TABLES = shopPlugin.getConfig().getStringList("shop.sql.createTables");

        establishDatabaseConnection();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            String createTableQuery = ""; //TODO: Add table query
            enqueueOperation(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public CompletableFuture<ResultSet> enqueueOperation(String data) {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        operationQueue.offer(Pair.of(data, future));
        shopPlugin.getServer().getScheduler().runTaskAsynchronously(shopPlugin, this::executeNextOperation);
        return future;
    }

    private void executeNextOperation() {
        if (!operationQueue.isEmpty()) {
            Pair<String, CompletableFuture<ResultSet>> operation = operationQueue.peek();
            String data = operation.left();
            ResultSet resultSet = DataBaseUtil.executeQuery(connection, data);
            operation.right().complete(resultSet);
            operationQueue.remove();
        }
    }

    public void establishDatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:plugins/" + shopPlugin.getDataFolder().getName() + "/database.db");
        } catch (SQLException | ClassNotFoundException e) {
            shopPlugin.getLogger().warning("Failed to connect to database");
            shopPlugin.getLogger().warning(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(shopPlugin);
        }
    }
}

