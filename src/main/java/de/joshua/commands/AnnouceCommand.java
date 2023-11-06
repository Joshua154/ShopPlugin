package de.joshua.commands;

import de.joshua.ShopPlugin;
import de.joshua.uis.BuyGUI;
import de.joshua.util.ShopDataBaseUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AnnouceCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public AnnouceCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!player.isOp()) {
            ShopPlugin.sendMessage(Component.text("You are not allowed to use this command"), player);
            return false;
        }
        if (args.length != 1) {
            ShopPlugin.sendMessage(Component.text("Invalid Arguments"), player);
            return false;
        }

        ShopPlugin.sendMessage(MiniMessage.miniMessage().deserialize("<click:run_command:/open " + args[0] + ">Click here to check this item out"), Bukkit.getOnlinePlayers().toArray(new Player[0]));

        return true;
    }
}
