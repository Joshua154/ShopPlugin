package de.joshua.util.dbItems;

import de.joshua.ShopPlugin;
import de.joshua.util.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

public record StoredItemDataBase(int dbID, ItemStack item, UUID seller, UUID buyer, LocalDateTime bought_at,
                                 int soldItemID) {
    public ItemStack getPreviewItem() {
        Component playerComponent;
        Player player = Bukkit.getServer().getOfflinePlayer(buyer()).getPlayer();
        if (player == null) {
            playerComponent = Component.text(ShopPlugin.getConfigString("shop.error.unknown")).color(NamedTextColor.WHITE);
        } else {
            playerComponent = player.teamDisplayName();
        }

        List<Component> lore = ShopPlugin.getConfigStringList("shop.item.storedItems.lore").stream()
                .map(s -> MiniMessage.miniMessage().deserialize(s,
                                Placeholder.component("bought_by", playerComponent),
                                Placeholder.component("time", Component.text(getFormattedDate()))
                        )
                )
                .toList();
        return new ItemBuilder(item())
                .lore(lore.toArray(Component[]::new))
                .build();
    }

    @Override
    public ItemStack item() {
        return item.clone();
    }

    private String getFormattedDate() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(bought_at());
    }
}
