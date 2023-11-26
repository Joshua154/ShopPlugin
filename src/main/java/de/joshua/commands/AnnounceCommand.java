package de.joshua.commands;

import de.joshua.ShopPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AnnounceCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public AnnounceCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length != 1) {
            ShopPlugin.sendMessage(Component.text(ShopPlugin.getConfigString("shop.error.invalidArgs")), player);
            return false;
        }
        sendAnnouncement(args[0], player.displayName());

        return true;
    }

    public static void sendAnnouncement(String itemID, Component playerName) {
        ShopPlugin.sendMessage(
                MiniMessage.miniMessage().deserialize(
                        ShopPlugin.getConfigString("shop.command.announce"),
                        Placeholder.component("player_name", playerName),
                        Placeholder.parsed("item_id", itemID)
                ),
                Bukkit.getOnlinePlayers().toArray(new Player[0]));
    }
}
