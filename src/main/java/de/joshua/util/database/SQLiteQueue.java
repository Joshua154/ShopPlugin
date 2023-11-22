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

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SQLiteQueue {
    private static List<String> DATABASE_TABLES = null;
    ShopPlugin shopPlugin;
    private final Queue<Pair<String, CompletableFuture<ResultSet>>> operationQueue;
    private Connection connection;
    private boolean isProcessing = false;

    public SQLiteQueue(ShopPlugin shopPlugin) {
        operationQueue = new LinkedList<>();
        this.shopPlugin = shopPlugin;
        DATABASE_TABLES = shopPlugin.getConfig().getStringList("shop.sql.createTables");

        initializeDatabase();
    }

    private void initializeDatabase() {
        for (String table : DATABASE_TABLES) {
            enqueueOperation(table);
        }
    }


    public synchronized CompletableFuture<ResultSet> enqueueOperation(String request) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<ResultSet> future = new CompletableFuture<>();
            Pair<String, CompletableFuture<ResultSet>> pair = Pair.of(request, future);
            operationQueue.add(pair);
            if (!isProcessing) {
                processQueue();
            }
            return future.join();
        });
    }

    private void processQueue() {
        while (!operationQueue.isEmpty()) {
            Pair<String, CompletableFuture<ResultSet>> pair = operationQueue.poll();
            String operation = pair.left();
            System.out.println(operation);
            CompletableFuture<ResultSet> future = pair.right();

            establishDatabaseConnection();
            ResultSet resultSet = DataBaseUtil.executeQuery(connection, operation).join();

            future.complete(resultSet);
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

