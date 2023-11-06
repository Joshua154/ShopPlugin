package de.joshua.util.dbItems;

import de.joshua.util.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.UUID;

public record StoredItemDataBase(int dbID, ItemStack item, UUID seller, UUID buyer, LocalDateTime bought_at, int soldItemID) {
    public ItemStack getPreviewItem() {
        return new ItemBuilder(item())
                .lore(Component.text("Buyer: " + Bukkit.getServer().getOfflinePlayer(buyer()).getName()),
                        Component.text("Created at: " + bought_at()))
                .build();
    }

    @Override
    public ItemStack item() {
        return item.clone();
    }
}
