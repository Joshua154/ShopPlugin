package de.joshua.util.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public class GUIEH implements Listener {
    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof IGUI igui)) return;
        if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() instanceof Player) return;
        if (event.getCurrentItem() == null) return;
        event.setCancelled(true);
        igui.onClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof IGUI igui)) return;
        igui.onClose(event);
    }
}
