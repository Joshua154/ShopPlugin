package de.joshua.util.dbItems;

import de.joshua.util.ShopDataBaseUtil;
import de.joshua.util.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.UUID;

public record OfferItemDataBase(int dbID, UUID seller, UUID offeredBy, ItemStack offer, LocalDateTime created_at, SellItemDataBase sellItem) {
    public ItemStack getPreviewItem() {
        return new ItemBuilder(sellItem.item())
                .lore(Component.text("Offer: " + Bukkit.getServer().getOfflinePlayer(seller()).getName()),
                        Component.text("Offered Price: " + offer().getType()),
                        Component.text("Created at: " + created_at()))
                .build();
    }

    @Override
    public ItemStack offer() {
        return offer.clone();
    }
}
