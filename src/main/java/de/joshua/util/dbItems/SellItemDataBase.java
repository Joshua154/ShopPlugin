package de.joshua.util.dbItems;

import de.joshua.ShopPlugin;
import de.joshua.util.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

public record SellItemDataBase(int dbID, ItemStack item, ItemStack price, UUID seller, LocalDateTime created_at) {
    public ItemStack getPreviewItem(NamespacedKey key) {
        Component playerComponent;
        Player player = Bukkit.getServer().getOfflinePlayer(seller()).getPlayer();
        if (player == null) {
            playerComponent = Component.text(ShopPlugin.getConfigString("shop.error.unknown")).color(NamedTextColor.WHITE);
        } else {
            playerComponent = player.teamDisplayName();
        }

        List<Component> lore = ShopPlugin.getConfigStringList("shop.item.sellItem.lore").stream()
                .map(s -> MiniMessage.miniMessage().deserialize(s,
                                Placeholder.component("price", Component.text(price().getType().name())),
                                Placeholder.parsed("price_trans_key", price().getType().translationKey()),
                                Placeholder.component("seller", playerComponent),
                                Placeholder.component("time", Component.text(getFormattedDate()))
                        )
                )
                .toList();
        return new ItemBuilder(item())
                .lore(lore.toArray(Component[]::new))
                .persistentData(key, PersistentDataType.INTEGER, dbID)
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

    private String getFormattedDate() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(created_at());
    }

    public boolean isNotAvailable(ShopPlugin shopPlugin) {
//        return !ShopDataBaseUtil.isStillAvailable(shopPlugin, dbID);
        return false;
    }
}
