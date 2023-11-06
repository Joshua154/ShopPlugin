package de.joshua.commands;

import de.joshua.ShopPlugin;
import de.joshua.uis.BuyGUI;
import de.joshua.uis.SellGUI;
import de.joshua.util.ShopDataBaseUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OppenBuyCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public OppenBuyCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length != 1) {
            ShopPlugin.sendMessage(Component.text("Invalid Arguments"), player);
            return false;
        }

        ShopDataBaseUtil.getSpecificSellItem(shopPlugin.getDatabaseConnection(), Integer.parseInt(args[0])).ifPresentOrElse(
                sellItemDataBase -> {
                    BuyGUI buyGUI = new BuyGUI(shopPlugin, sellItemDataBase, player);
                    buyGUI.open();
                },
                () -> ShopPlugin.sendMessage(Component.text("This item is not available"), player));

        return true;
    }
}
