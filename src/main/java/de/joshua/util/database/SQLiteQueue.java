package de.joshua.util.database;

import de.joshua.ShopPlugin;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class SQLiteQueue {
    private static List<String> DATABASE_TABLES = null;
    private final Queue<Pair<String, CompletableFuture<DataBaseCollection>>> operationQueue;
    private final boolean isProcessing = false;
    ShopPlugin shopPlugin;
    private Connection connection;

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


    public synchronized CompletableFuture<DataBaseCollection> enqueueOperation(String request) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<DataBaseCollection> future = new CompletableFuture<>();
            Pair<String, CompletableFuture<DataBaseCollection>> pair = Pair.of(request, future);
            operationQueue.add(pair);
            if (!isProcessing) {
                processQueue();
            }
            return future.join();
        });
    }

    private void processQueue() {
        while (!operationQueue.isEmpty()) {
            Pair<String, CompletableFuture<DataBaseCollection>> pair = operationQueue.poll();
            if (pair == null) continue;
            String operation = pair.left();
            System.out.println(operation);
            CompletableFuture<DataBaseCollection> future = pair.right();

            establishDatabaseConnection();
            DataBaseCollection dataBaseCollection = DataBaseUtil.executeQuery(connection, operation).join();

            future.complete(dataBaseCollection);
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

