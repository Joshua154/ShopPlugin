package de.joshua.util.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@SuppressWarnings("unused")
public class HeadBuilder implements ItemStackBuilder<HeadBuilder> {

    private final ItemStack itemStack;
    private final SkullMeta meta;

    public HeadBuilder() {
        this.itemStack = new ItemStack(Material.PLAYER_HEAD);
        meta = (SkullMeta) itemStack.getItemMeta();
    }

    public HeadBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        if (itemStack.getType() != Material.PLAYER_HEAD)
            throw new IllegalStateException("type must be PLAYER_HEAD");
        this.meta = (SkullMeta) this.itemStack.getItemMeta();
    }

    @Override
    public HeadBuilder itemFlags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    @Override
    public HeadBuilder displayName(Component component) {
        meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(component));
        return this;
    }

    @Override
    public HeadBuilder lore(Component... components) {
        meta.lore(Arrays.asList(components));
        return this;
    }

    @Override
    public HeadBuilder lore(ArrayList<Component> components) {
        meta.lore(components);
        return this;
    }

    @Override
    public HeadBuilder amount(int amount) {
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
    public HeadBuilder persistentData(NamespacedKey namespacedKey, PersistentDataType type, Object value) {
        meta.getPersistentDataContainer().set(namespacedKey, type, value);
        return this;
    }

    public HeadBuilder headOwner(UUID headOwner) {
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(headOwner));
        return this;
    }


    public HeadBuilder skullOwner(@NotNull String url) {
//        String finalUrl = "";
//        try {
//            UUID uuid = UUID.fromString(url);
//            finalUrl = TextureFetcher.getSkinUrl(uuid.toString());
//        } catch (Exception ignored) {
//        }
//        if (url.length() <= 16) {
//            UUID uuid = UUIDFetcher.getUUID(url);
//            if (uuid == null)
//                finalUrl = "https://textures.minecraft.net/texture/" +
//                        "647cf0f3b9ec9df2485a9cd4795b60a391c8e6ebac96354de06e3357a9a88607";
//            else finalUrl = TextureFetcher.getSkinUrl(uuid.toString());
//        } else if (finalUrl.isEmpty()) {
//            finalUrl = "https://textures.minecraft.net/texture/" + url;
//        }
//
//        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
//        byte[] data = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", finalUrl).getBytes());
//        gameProfile.getProperties().put("textures", new Property("textures", new String(data)));
//        try {
//            Field field = meta.getClass().getDeclaredField("profile");
//            field.setAccessible(true);
//            field.set(meta, gameProfile);
//            field.setAccessible(false);
//        } catch (Exception ignored) {
//        }
        //TODO
        return this;
    }


    @Override
    public HeadBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    @Override
    public HeadBuilder unEnchant(Enchantment enchantment) {
        meta.removeEnchant(enchantment);
        return this;
    }

    @Override
    public ItemStack build() {
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
