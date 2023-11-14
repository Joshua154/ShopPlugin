package de.joshua.commands;

import de.joshua.ShopPlugin;
import de.joshua.uis.StoredItemsGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StoredCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public StoredCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        StoredItemsGUI sellGUI = new StoredItemsGUI(shopPlugin, player);
        sellGUI.open();

        return true;
    }
}
