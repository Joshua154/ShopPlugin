package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.uis.sellection.ItemSelection;
import de.joshua.util.ShopDataBaseUtil;
import de.joshua.util.ShopUtil;
import de.joshua.util.item.ItemBuilder;
import de.joshua.util.ui.IGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SellGUI implements IGUI {
    private final Map<Integer, SellItemType> itemMap = new HashMap<>();
    private final Map<SellItemType, ItemStack> collectedItems = new HashMap<>();
    private final Player player;
    ShopPlugin shopPlugin;
    private Inventory inventory;
    private int quantityOverwrite = 0;
    private Material materialOverwrite = null;

    public SellGUI(ShopPlugin shopPlugin, Player player) {
        this.shopPlugin = shopPlugin;
        this.player = player;

        itemMap.put(9 + 2, SellItemType.SELL_ITEM);
        itemMap.put(18 + 2, SellItemType.SET_PRICE);
    }

    protected NamespacedKey getGUIKey(String key) {
        return new NamespacedKey("joshua.gui.item", key);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta.getPersistentDataContainer().has(getGUIKey("sell_gui"), PersistentDataType.STRING)) {
            String property = meta.getPersistentDataContainer().get(getGUIKey("sell_gui"), PersistentDataType.STRING);
            if (Objects.requireNonNull(property).equals("button")) {
                switch (Objects.requireNonNull(meta.getPersistentDataContainer().get(getGUIKey("type"), PersistentDataType.STRING))) {
                    case "confirm" -> {
                        parseItems();
                        acceptItems();
                        player.closeInventory();
                    }
                    case "cancel" -> {
                        parseItems();
                        giveItemsBack();
                        player.closeInventory();
                    }
                    case "select_price" -> {
                        parseItems();
                        inventory.setItem(9 + 2, new ItemStack(Material.AIR));
                        inventory.setItem(9 + 9 + 2, new ItemStack(Material.AIR));
                        new ItemSelection(shopPlugin, player, this).open();
                    }
                }
            }
        } else event.setCancelled(false);
    }

    @Override
    public void open(Player player) {
        this.inventory = getInventory();
        player.openInventory(this.inventory);
        updatePreviewItem();
    }

    public void open() {
        this.open(this.player);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        parseItems();
        giveItemsBack();
    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, 9 * 4, Component.text("Sell"));

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                    .displayName(Component.text(" "))
                    .persistentData(getGUIKey("sell_gui"), PersistentDataType.STRING, "background")
                    .build());
        }

        inventory.setItem(9 + 1, new ItemBuilder(Material.PAPER)
                .displayName(Component.text("Item to Sell"))
                .persistentData(getGUIKey("sell_gui"), PersistentDataType.STRING, "property")
                .build());
        inventory.setItem(9 + 2, new ItemStack(Material.AIR));
        if (collectedItems.get(SellItemType.SELL_ITEM) != null) {
            inventory.setItem(9 + 2, collectedItems.get(SellItemType.SELL_ITEM));
        }

        inventory.setItem(9 + 9 + 1, new ItemBuilder(Material.PAPER)
                .displayName(Component.text("Custom Price"))
                .persistentData(getGUIKey("sell_gui"), PersistentDataType.STRING, "property")
                .build());
        inventory.setItem(9 + 9 + 2, new ItemStack(Material.AIR));
        if (collectedItems.get(SellItemType.SET_PRICE) != null) {
            inventory.setItem(9 + 9 + 2, collectedItems.get(SellItemType.SET_PRICE));
        }


        inventory.setItem(9 + 9 + 3, new ItemBuilder(Material.CHEST)
                .displayName(Component.text("Select Price"))
                .persistentData(getGUIKey("sell_gui"), PersistentDataType.STRING, "button")
                .persistentData(getGUIKey("type"), PersistentDataType.STRING, "select_price")
                .build());


        inventory.setItem(9 + 9 + 5, new ItemBuilder(Material.LIME_CONCRETE)
                .displayName(Component.text("Confirm"))
                .persistentData(getGUIKey("sell_gui"), PersistentDataType.STRING, "button")
                .persistentData(getGUIKey("type"), PersistentDataType.STRING, "confirm")
                .build());
        inventory.setItem(9 + 9 + 6, new ItemBuilder(Material.RED_CONCRETE)
                .displayName(Component.text("Cancel"))
                .persistentData(getGUIKey("sell_gui"), PersistentDataType.STRING, "button")
                .persistentData(getGUIKey("type"), PersistentDataType.STRING, "cancel")
                .build());

        return inventory;
    }

    private void acceptItems() {
        ItemStack sellItem = collectedItems.get(SellItemType.SELL_ITEM);
        ItemStack sellPrice = collectedItems.get(SellItemType.SET_PRICE);
        if (sellItem == null) {
            giveItemsBack();
            ShopPlugin.sendMessage(Component.text("Error: No item to sell!"), player);
            return;
        }
        if (sellPrice == null) {
            giveItemsBack();
            ShopPlugin.sendMessage(Component.text("Error: No item for Price!"), player);
            return;
        }

        String playerUUID = player.getUniqueId().toString();

        ItemStack priceItem = collectedItems.get(SellItemType.SET_PRICE);
        ItemMeta meta = priceItem.getItemMeta();
        meta.getPersistentDataContainer().remove(getGUIKey("sell_gui"));
        priceItem.setItemMeta(meta);

        Bukkit.getScheduler().runTaskAsynchronously(shopPlugin, () -> ShopDataBaseUtil.addNewSellItem(shopPlugin.getDatabaseConnection(), priceItem, sellPrice, playerUUID));

        collectedItems.remove(SellItemType.SELL_ITEM);

        giveItemsBack();
    }

    private void giveItemsBack() {
        if (collectedItems.isEmpty()) return;
        List<SellItemType> toRemove = new ArrayList<>();

        for (Map.Entry<SellItemType, ItemStack> entry : collectedItems.entrySet()) {
            if (entry.getValue() == null) continue;
            if (!(new ItemBuilder(entry.getValue()).hasPersistentData(getGUIKey("sell_gui"), PersistentDataType.STRING))) {
                ShopUtil.addItemToInventory(player, entry.getValue());
            }
            toRemove.add(entry.getKey());
        }

        for (SellItemType sellItemType : toRemove) {
            collectedItems.remove(sellItemType);
        }
    }

    private void parseItems() {
        Map<SellItemType, ItemStack> tmp = new HashMap<>();
        for (Map.Entry<Integer, SellItemType> entry : itemMap.entrySet()) {
            ItemStack item = inventory.getItem(entry.getKey());
            if (item == null) continue;
            inventory.setItem(entry.getKey(), new ItemStack(Material.AIR));
            tmp.put(entry.getValue(), item);
        }
        collectedItems.putAll(tmp);
    }

    public void setMaterial(Material material) {
        System.out.println("Material: " + material);
        this.materialOverwrite = material;
    }

    public void setQuantity(Integer itemQuantity) {
        this.quantityOverwrite = itemQuantity;
    }

    public void updatePreviewItem() {
        System.out.println(materialOverwrite);
        System.out.println(quantityOverwrite);

        boolean modifiedMaterial = false;

        parseItems();
        if (collectedItems.get(SellItemType.SET_PRICE) != null) {
            ShopUtil.addItemToInventory(player, collectedItems.get(SellItemType.SET_PRICE));
            if (materialOverwrite == null) {
                materialOverwrite = Material.STONE;
                modifiedMaterial = true;
            }
        }

        if (materialOverwrite != null && quantityOverwrite != 0) {
            itemMap.entrySet().stream().filter(entry -> entry.getValue().equals(SellItemType.SET_PRICE)).findFirst().ifPresent(entry ->
                    inventory.setItem(entry.getKey(), new ItemBuilder(materialOverwrite)
                            .persistentData(getGUIKey("sell_gui"), PersistentDataType.STRING, "selected_item")
                            .amount(quantityOverwrite)
                            .build())
            );
        }

        if (modifiedMaterial) materialOverwrite = null;
    }
}
