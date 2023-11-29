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

public record OfferItemDataBase(int dbID, UUID seller, UUID offeredBy, ItemStack offer, LocalDateTime created_at,
                                SellItemDataBase sellItem) {
    public ItemStack getPreviewItem() {
        Component playerComponent;
        Player player = Bukkit.getServer().getOfflinePlayer(offeredBy()).getPlayer();
        if (player == null) {
            playerComponent = Component.text(ShopPlugin.getConfigString("shop.error.unknown")).color(NamedTextColor.WHITE);
        } else {
            playerComponent = player.teamDisplayName();
        }

        List<Component> lore = ShopPlugin.getConfigStringList("shop.item.offered.lore").stream()
                .map(s -> MiniMessage.miniMessage().deserialize(s,
                                Placeholder.component("price", Component.text(offer().getType().name())),
                                Placeholder.parsed("price_trans_key", offer().getType().translationKey()),
                                Placeholder.component("offered_by", playerComponent),
                                Placeholder.component("time", Component.text(getFormattedDate()))
                        )
                )
                .toList();
        return new ItemBuilder(sellItem.item())
                .lore(lore.toArray(Component[]::new))
                .build();
    }

    @Override
    public ItemStack offer() {
        return offer.clone();
    }

    private String getFormattedDate() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(created_at());
    }
}
