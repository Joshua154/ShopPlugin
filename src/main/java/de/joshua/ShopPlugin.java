package de.joshua;

import de.joshua.commands.AnnounceCommand;
import de.joshua.commands.OppenBuyCommand;
import de.joshua.commands.RunSQLCommand;
import de.joshua.commands.ShopCommand;
import de.joshua.util.DiscordWebhook;
import de.joshua.util.database.SQLiteQueue;
import de.joshua.util.ui.GUIEH;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ShopPlugin extends JavaPlugin {
    public static UUID[] SQL_UUIDS;
    private SQLiteQueue sqlQueue;
    private static ShopPlugin instance;

    public static void sendMessage(Component component, Player... players) {
        Component msg = getPrefix().append(component);
        for (Player player : players) {
            player.sendMessage(msg);
        }
    }

    public static Component getPrefix() {
        return MiniMessage.miniMessage().deserialize(ShopPlugin.getConfigString("shop.prefix")).append(Component.space());
    }

    @Override
    public void onLoad() {
        instance = this;
        this.saveDefaultConfig();

        SQL_UUIDS = getConfig().getStringList("shop.sql.uuids").stream().map(UUID::fromString).toArray(UUID[]::new);
    }

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        this.sqlQueue = new SQLiteQueue(this);
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
        Objects.requireNonNull(getCommand("announce")).setExecutor(new AnnounceCommand(this));
        Objects.requireNonNull(getCommand("executeSQL")).setExecutor(new RunSQLCommand(this));
//        Objects.requireNonNull(getCommand("offers")).setExecutor(new SeeOffersCommand(this));
    }

    public SQLiteQueue getSQLQueue() {
        return sqlQueue;
    }

    public static DiscordWebhook getDiscordWebhook() {
        return new DiscordWebhook("https://discord.com/api/webhooks/1175339451157843978/xi7bUSgGW8GbjpRckep3SaObq-Cu-ob4l-u6BgG9WfTh7GcFSy68ObimnXwvqliliZDG");
    }

    public static ShopPlugin getInstance() {
        return instance;
    }

    public static FileConfiguration getFileConfig() {
        return instance.getConfig();
    }

    @NotNull
    public static String getConfigString(String key) {
        return getFileConfig().getString(key) == null ? "Err" : Objects.requireNonNull(getFileConfig().getString(key));
    }

    public static List<String> getConfigStringList(String key) {
        return getFileConfig().getString(key) == null ? List.of("Err") : Objects.requireNonNull(getFileConfig().getStringList(key));
    }
}
