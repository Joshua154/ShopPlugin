package de.joshua.util.item;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

@SuppressWarnings({"rawtypes", "unused"})
public interface ItemStackBuilder<B extends ItemStackBuilder> {

    B displayName(Component component);

    B lore(Component... components);

    B lore(ArrayList<Component> components);

    B amount(int amount);

    Object persistentData(PersistentDataType type, NamespacedKey namespacedKey);

    B persistentData(NamespacedKey namespacedKey, PersistentDataType type, Object value);

    boolean hasPersistentData(NamespacedKey namespacedKey, PersistentDataType type);

    B itemFlags(ItemFlag... flags);

    B enchant(Enchantment enchantment, int level);

    B unEnchant(Enchantment enchantment);

    ItemStack build();

}
