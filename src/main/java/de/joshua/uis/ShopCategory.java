package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.util.dbItems.SellItemDataBase;
import de.joshua.util.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public enum ShopCategory {
    BUY_MATERIAL(new ItemBuilder(Material.BRICKS)
            .displayName(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.name"))
            .lore(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.sell")).build(), (sellItem) -> sellItem.stream().sorted(
            (itemStack1, itemStack2) -> {
                String material1 = itemStack1.item().getType().name();
                String  material2 = itemStack2.item().getType().name();
                return material1.compareTo(material2);
            }
    ).toList()),
    PRICE_MATERIAL(new ItemBuilder(Material.BRICKS)
            .displayName(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.name"))
            .lore(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.price")).build(), (sellItem) -> sellItem.stream().sorted(
            (itemStack1, itemStack2) -> {
                String material1 = itemStack1.price().getType().name();
                String  material2 = itemStack2.price().getType().name();
                return material1.compareTo(material2);
            }
    ).toList()),
    NEW_TO_OLD(new ItemBuilder(Material.CLOCK)
            .displayName(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.name"))
            .lore(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.oldTnew")).build(), (sellItem) -> sellItem.stream().sorted(
            (itemStack1, itemStack2) -> {
                LocalDateTime date1 = itemStack1.created_at();
                LocalDateTime date2 = itemStack2.created_at();
                return date1.compareTo(date2);
            }
    ).toList()),
    OLD_TO_NEW(new ItemBuilder(Material.CLOCK)
            .displayName(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.name"))
            .lore(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.newTold")).build(), (sellItem) -> sellItem.stream().sorted(
            (itemStack1, itemStack2) -> {
                LocalDateTime date1 = itemStack1.created_at();
                LocalDateTime date2 = itemStack2.created_at();
                return date1.compareTo(date2) * -1;
            }
    ).toList());


    private final ItemStack displayItem;
    private final Operation parseItems;

    ShopCategory(ItemStack displayItem, Operation parseItems) {
        this.displayItem = displayItem;
        this.parseItems = parseItems;
    }

    public List<SellItemDataBase> parseItems(List<SellItemDataBase> itemStacks) {
        return parseItems.apply(itemStacks);
    }

    ShopCategory next() {
        return values()[(ordinal() + 1) % values().length];
    }

    ShopCategory previous() {
        return values()[(ordinal() - 1) % values().length];
    }

    public static ShopCategory getFirst() {
        return values()[0];
    }

    public ItemStack displayItem() {
        return displayItem;
    }
}

interface Operation {
    List<SellItemDataBase> apply(List<SellItemDataBase> itemStacks);
}