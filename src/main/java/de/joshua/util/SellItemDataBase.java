package de.joshua.util;

import de.joshua.util.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.UUID;

public record SellItemDataBase(int dbID, ItemStack item, ItemStack price, UUID seller, LocalDateTime created_at) {
    public ItemStack getPreviewItem() {
        return new ItemBuilder(item())
                .lore(Component.text("Seller: " + Bukkit.getServer().getOfflinePlayer(seller()).getName()),
                        Component.text("Price: " + price().getType()),
                        Component.text("Created at: " + created_at()))
                .build();
    }

    @Override
    public ItemStack item() {
        return item.clone();
    }

    @Override
    public ItemStack price() {
        return price.clone();
    }

    @Override
    public UUID seller() {
        return seller;
    }

    @Override
    public LocalDateTime created_at() {
        return created_at;
    }

    public boolean isStillAvailable(Connection connection) {
        return ShopDataBaseUtil.isStillAvailable(connection, dbID);
    }
}
