package de.joshua.util.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public interface IGUI extends InventoryHolder {
    void onClick(InventoryClickEvent event);

    void open(Player player);

    void onClose(InventoryCloseEvent event);
}
