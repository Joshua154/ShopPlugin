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
import org.bukkit.Bukkit;
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
    private static ShopPlugin instance;
    private SQLiteQueue sqlQueue;

    public static void sendMessage(Component component, Player... players) {
        Component msg = getPrefix().append(component);
        for (Player player : players) {
            player.sendMessage(msg);
        }
    }

    public static void sendMessageToAdmin(Component component) {
        Component msg = getPrefix().append(component);
        for (Player player : Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("shop.admin")).toList()) {
            player.sendMessage(msg);
        }
    }

    public static Component getPrefix() {
        return MiniMessage.miniMessage().deserialize(ShopPlugin.getConfigString("shop.prefix")).append(Component.space());
    }

    public static DiscordWebhook getDiscordWebhook() {
        return new DiscordWebhook(ShopPlugin.getConfigString("discord.debugWebhook"));
    }

    public static FileConfiguration getFileConfig() {
        return instance.getConfig();
    }

    @NotNull
    public static String getConfigString(String key) {
        return getFileConfig().getString(key) == null ? "Err" : Objects.requireNonNull(getFileConfig().getString(key));
    }

    @NotNull
    public static Component getConfigStringParsed(String key) {
        return MiniMessage.miniMessage().deserialize(getConfigString(key));
    }

    public static List<String> getConfigStringList(String key) {
        return getFileConfig().getString(key) == null ? List.of("Err") : Objects.requireNonNull(getFileConfig().getStringList(key));
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
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand(this));
        Objects.requireNonNull(getCommand("open")).setExecutor(new OppenBuyCommand(this));
        Objects.requireNonNull(getCommand("announce")).setExecutor(new AnnounceCommand(this));
        Objects.requireNonNull(getCommand("executeSQL")).setExecutor(new RunSQLCommand(this));
    }

    public SQLiteQueue getSQLQueue() {
        return sqlQueue;
    }
}
