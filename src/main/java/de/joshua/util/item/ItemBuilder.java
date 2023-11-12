package de.joshua.util.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
@NotNull
public class ItemBuilder implements ItemStackBuilder<ItemBuilder> {

    private final ItemStack itemStack;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material == null ? Material.AIR : material);
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder() {
        this.itemStack = new ItemStack(Material.AIR);
        this.meta = null;
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = this.itemStack.getItemMeta();

    }

    @Override
    public ItemBuilder itemFlags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    @Override
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    @Override
    public ItemBuilder unEnchant(Enchantment enchantment) {
        meta.removeEnchant(enchantment);
        return this;
    }

    @Override
    public ItemBuilder displayName(Component component) {
        meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(component));
        return this;
    }

    @Override
    public ItemBuilder lore(Component... components) {
        meta.lore(Arrays.asList(components));
        return this;
    }

    @Override
    public ItemBuilder lore(ArrayList<Component> components) {
        meta.lore(components);
        return this;
    }

    @Override
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    @Override
    public Object persistentData(PersistentDataType type, NamespacedKey namespacedKey) {
        return meta.getPersistentDataContainer().get(namespacedKey, type);
    }

    @Override
    public boolean hasPersistentData(NamespacedKey namespacedKey, PersistentDataType type) {
        return meta.getPersistentDataContainer().has(namespacedKey, type);
    }

    @Override
    public ItemBuilder persistentData(NamespacedKey namespacedKey, PersistentDataType type, Object value) {
        meta.getPersistentDataContainer().set(namespacedKey, type, value);
        return this;
    }

    @Override
    public ItemStack build() {

        if (this.meta != null)
            itemStack.setItemMeta(meta);
        return itemStack;
    }

    public String toString() {
        if (this.meta != null)
            itemStack.setItemMeta(meta);
        return itemStack.toString();
    }

    public ItemBuilder customModelData(int data) {
        meta.setCustomModelData(data);
        return this;
    }
}
