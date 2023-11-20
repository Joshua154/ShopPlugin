package de.joshua;

import de.joshua.commands.AnnouceCommand;
import de.joshua.commands.OppenBuyCommand;
import de.joshua.commands.RunSQLCommand;
import de.joshua.commands.ShopCommand;
import de.joshua.util.DiscordWebhook;
import de.joshua.util.ui.GUIEH;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public final class ShopPlugin extends JavaPlugin {
    public static final UUID[] SQL_UUIDS = new UUID[]{
            UUID.fromString("596b9acc-d337-4bed-a7a5-7c407d2938cf")
    };
    private Connection databaseConnection;

    public static void sendMessage(Component component, Player... players) {
        Component msg = getPrefix().append(component);
        for (Player player : players) {
            player.sendMessage(msg);
        }
    }

    public static Component getPrefix() {
        return MiniMessage.miniMessage().deserialize("<gray>[<bold><gradient:#ff930f:#fff95b>Shop</gradient></bold>]</gray><white>").append(Component.space());
    }

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        establishDatabaseConnection();
        registerEvents();
        registerCommands();
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new GUIEH(), this);
    }

    private void registerCommands() {
//        Objects.requireNonNull(getCommand("sell")).setExecutor(new SellCommand(this));
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand(this));
//        Objects.requireNonNull(getCommand("stored")).setExecutor(new StoredCommand(this));
        Objects.requireNonNull(getCommand("open")).setExecutor(new OppenBuyCommand(this));
        Objects.requireNonNull(getCommand("announce")).setExecutor(new AnnouceCommand(this));
        Objects.requireNonNull(getCommand("executeSQL")).setExecutor(new RunSQLCommand(this));
//        Objects.requireNonNull(getCommand("offers")).setExecutor(new SeeOffersCommand(this));
    }

    private void establishDatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:plugins/" + getDataFolder().getName() + "/database.db");
        } catch (SQLException | ClassNotFoundException e) {
            getLogger().warning("Failed to connect to database");
            getLogger().warning(e.getMessage());
        }
    }

    public Connection getDatabaseConnection() {
        return databaseConnection;
    }

    public static DiscordWebhook getDiscordWebhook() {
        return new DiscordWebhook("https://discord.com/api/webhooks/1175339451157843978/xi7bUSgGW8GbjpRckep3SaObq-Cu-ob4l-u6BgG9WfTh7GcFSy68ObimnXwvqliliZDG");
    }
}
