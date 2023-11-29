package de.joshua.commands;

import de.joshua.ShopPlugin;
import de.joshua.uis.BuyGUI;
import de.joshua.util.database.ShopDataBaseUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OppenBuyCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public OppenBuyCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length != 1) {
            ShopPlugin.sendMessage(Component.text(ShopPlugin.getConfigString("shop.error.invalidArgs")), player);
            return false;
        }

        ShopDataBaseUtil.getSpecificSellItem(shopPlugin, Integer.parseInt(args[0])).join().ifPresentOrElse(
                sellItemDataBase -> {
                    BuyGUI buyGUI = new BuyGUI(shopPlugin, sellItemDataBase, player);
                    buyGUI.open();
                },
                () -> ShopPlugin.sendMessage(Component.text(ShopPlugin.getConfigString("shop.error.itemUnavailable")), player));

        return true;
    }
}
