package de.joshua.util.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public interface IGUI extends InventoryHolder {
    void onClick(InventoryClickEvent event);

    void open(Player player);

    void onClose(Player player, Inventory inventory);
}
