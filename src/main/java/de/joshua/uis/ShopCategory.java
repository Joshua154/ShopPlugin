package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.util.LanguageUTILS;
import de.joshua.util.dbItems.SellItemDataBase;
import de.joshua.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public enum ShopCategory {
    BUY_MATERIAL(new ItemBuilder(Material.BRICKS)
            .displayName(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.name"))
            .lore(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.sell")).build(), (sellItem, languageKey) -> sellItem.stream().sorted(
            (itemStack1, itemStack2) -> {
                String material1 = ShopPlugin.getTranslatedItemName(itemStack1.item().getType(), languageKey);
                String material2 = ShopPlugin.getTranslatedItemName(itemStack2.item().getType(), languageKey);
                return material1.compareTo(material2);
            }
    ).toList()),
    PRICE_MATERIAL(new ItemBuilder(Material.BRICKS)
            .displayName(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.name"))
            .lore(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.price")).build(), (sellItem, languageKey) -> sellItem.stream().sorted(
            (itemStack1, itemStack2) -> {
                String material1 = ShopPlugin.getTranslatedItemName(itemStack1.price().getType(), languageKey);
                String material2 = ShopPlugin.getTranslatedItemName(itemStack2.price().getType(), languageKey);
                return material1.compareTo(material2);
            }
    ).toList()),
    NEW_TO_OLD(new ItemBuilder(Material.CLOCK)
            .displayName(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.name"))
            .lore(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.oldTnew")).build(), (sellItem, languageKey) -> sellItem.stream().sorted(
            (itemStack1, itemStack2) -> {
                LocalDateTime date1 = itemStack1.created_at();
                LocalDateTime date2 = itemStack2.created_at();
                return date1.compareTo(date2);
            }
    ).toList()),
    OLD_TO_NEW(new ItemBuilder(Material.CLOCK)
            .displayName(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.name"))
            .lore(ShopPlugin.getConfigStringParsed("shop.shop.categories.item.newTold")).build(), (sellItem, languageKey) -> sellItem.stream().sorted(
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

    public static ShopCategory getFirst() {
        return values()[0];
    }

    public List<SellItemDataBase> parseItems(List<SellItemDataBase> itemStacks, String languageKey) {
        return parseItems.apply(itemStacks, languageKey);
    }

    ShopCategory next() {
        return values()[(ordinal() + 1) % values().length];
    }

    ShopCategory previous() {
        ShopCategory[] categories = values();
        int length = categories.length;
        return categories[(ordinal() + length - 1) % length];
    }

    public ItemStack displayItem() {
        return displayItem;
    }
}

interface Operation {
    List<SellItemDataBase> apply(List<SellItemDataBase> itemStacks, String languageKey);
}