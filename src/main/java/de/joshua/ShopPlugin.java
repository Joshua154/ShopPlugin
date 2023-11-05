package de.joshua;

import de.joshua.commands.SellCommand;
import de.joshua.commands.ShopCommand;
import de.joshua.commands.StoredCommand;
import de.joshua.util.ui.GUIEH;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public final class ShopPlugin extends JavaPlugin {
    private Connection databaseConnection;

    @Override
    public void onEnable() {
        establishDatabaseConnection();
        registerEvents();
        registerCommands();
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new GUIEH(), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("sell")).setExecutor(new SellCommand(this));
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand(this));
        Objects.requireNonNull(getCommand("stored")).setExecutor(new StoredCommand(this));
    }

    private void establishDatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:plugins\\" + getDataFolder().getName() + "\\database.db");
        } catch (SQLException | ClassNotFoundException e) {
            getLogger().warning("Failed to connect to database");
            getLogger().warning(e.getMessage());
        }
    }

    public Connection getDatabaseConnection() {
        return databaseConnection;
    }
}
