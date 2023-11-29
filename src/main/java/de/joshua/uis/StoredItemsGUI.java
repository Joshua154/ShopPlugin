package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.util.ShopUtil;
import de.joshua.util.database.ShopDataBaseUtil;
import de.joshua.util.dbItems.StoredItemDataBase;
import de.joshua.util.item.ItemBuilder;
import de.joshua.util.ui.PageGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class StoredItemsGUI extends PageGUI {
    ShopPlugin shopPlugin;
    List<StoredItemDataBase> db_items;

    public StoredItemsGUI(ShopPlugin shopPlugin, Player player) {
        super(Component.text(ShopPlugin.getConfigString("shop.storedItems.gui.name")));
        super.player = player;
        this.shopPlugin = shopPlugin;
        updateItems();
    }


    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    public void open() {
        super.open(player);
    }

    @Override
    public List<ItemStack> getContent() {
        return db_items.stream().map(this::generateItem).toList();
    }

    private ItemStack generateItem(StoredItemDataBase storedItemDataBase) {
        return new ItemBuilder(storedItemDataBase.getPreviewItem())
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "stored_item")
                .persistentData(getPageGUIKey("id"), PersistentDataType.INTEGER, storedItemDataBase.dbID())
                .build();
    }

    @Override
    public void onItemClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;
        if (!clickedItem.hasItemMeta()) return;
        if (!clickedItem.getItemMeta().getPersistentDataContainer().has(getPageGUIKey("type"), PersistentDataType.STRING))
            return;
        if (Objects.equals(clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("type"), PersistentDataType.STRING), "go_back_gui")) {
            handleBack();
            return;
        }
        if (!Objects.equals(clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("type"), PersistentDataType.STRING), "stored_item"))
            return;

        int id = Objects.requireNonNull(clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("id"), PersistentDataType.INTEGER));
        StoredItemDataBase sIDB = db_items.stream().filter(item -> item.dbID() == id).findFirst().orElse(null);
        if (sIDB == null) return;

        clickedItem.setItemMeta(sIDB.item().getItemMeta());

        ShopUtil.addItemToInventory(player, sIDB.item());

        Bukkit.getScheduler().runTaskAsynchronously(shopPlugin, () -> ShopDataBaseUtil.removeStoredItem(shopPlugin, sIDB.dbID()));

        db_items.remove(sIDB);
        updateCachedContent();
        refresh();
    }

    private void handleBack() {
        new ShopGUI(shopPlugin).open(player);
    }

    @Override
    public void onPageSwitch() {
        updateItems();
        updateCachedContent();
    }

    private void updateItems() {
        CompletableFuture<List<StoredItemDataBase>> future = ShopDataBaseUtil.getStoredItems(shopPlugin, super.player.getUniqueId());
        db_items = future.join();
    }

    @Override
    public @NotNull Inventory getInventory() {
        String goBack = ShopPlugin.getConfigString("shop.storedItems.gui.button.back");

        Inventory inventory = super.getInventory();
        inventory.setItem(9 * 5 + 4, new ItemBuilder(Material.RED_CONCRETE)
                .displayName(Component.text(goBack))
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "go_back_gui")
                .build());
        return inventory;
    }
}

