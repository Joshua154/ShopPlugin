package de.joshua.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ShopUtil {
    public static void addItemToInventory(Player player, ItemStack itemStack) {
        if (isInventoryFull(player.getInventory())) {
            player.getWorld().dropItem(player.getLocation(), itemStack);
        } else {
            player.getInventory().addItem(itemStack);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2F, 1.8F);
    }

    public static boolean isInventoryFull(PlayerInventory inventory) {
        boolean hasSpace = false;
        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null) {
                hasSpace = true;
                break;
            }
        }
        return !hasSpace;
    }
}
