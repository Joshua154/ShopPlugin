package de.joshua.commands;

import de.joshua.ShopPlugin;
import de.joshua.uis.ShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public ShopCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        ShopGUI shopGUI = new ShopGUI(shopPlugin, player);
        shopGUI.open();

        return true;
    }
}
