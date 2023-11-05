package de.joshua.commands;

import de.joshua.ShopPlugin;
import de.joshua.uis.SellGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SellCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public SellCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!player.isOp()) {
            player.sendMessage("You are not allowed to use this command");
            return false;
        }

//        ItemStack item = player.getInventory().getItemInMainHand();
//        Class<?> data = item.getType().data;
//        player.sendMessage(Component.text(data.getName()));
//        player.sendMessage(Component.text(Arrays.toString(data.getDeclaredFields())));


        SellGUI sellGUI = new SellGUI(shopPlugin, player);
        sellGUI.open();

        return true;
    }
}
