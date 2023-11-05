package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.util.ShopDataBaseUtil;
import de.joshua.util.StoredItemDataBase;
import de.joshua.util.item.ItemBuilder;
import de.joshua.util.ui.PageGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

public class StoredItemsGUI extends PageGUI {
    ShopPlugin shopPlugin;
    List<StoredItemDataBase> db_items;

    public StoredItemsGUI(ShopPlugin shopPlugin, Player player) {
        super(Component.text("Stored Items"));
        super.player = player;
        this.shopPlugin = shopPlugin;
        updateItems();
    }


    @Override
    public void onClose(Player player, Inventory inventory) {

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
        if (!Objects.equals(clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("type"), PersistentDataType.STRING), "stored_item"))
            return;

        int id = Objects.requireNonNull(clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("id"), PersistentDataType.INTEGER));
        StoredItemDataBase sIDB = db_items.stream().filter(item -> item.dbID() == id).findFirst().orElse(null);
        if (sIDB == null) return;

        clickedItem.setItemMeta(sIDB.item().getItemMeta());

        if (isInventoryFull(player.getInventory())) {
            player.getWorld().dropItem(player.getLocation(), sIDB.item());
        } else {
            player.getInventory().addItem(sIDB.item());
        }

        Bukkit.getScheduler().runTaskAsynchronously(shopPlugin, () -> ShopDataBaseUtil.removeStoredItem(shopPlugin.getDatabaseConnection(), sIDB.dbID()));

        db_items.remove(sIDB);
        refresh();
    }

    @Override
    public void onPageSwitch() {
        updateItems();
    }

    private void updateItems() {
        db_items = ShopDataBaseUtil.getStoredItems(shopPlugin.getDatabaseConnection(), super.player.getUniqueId());
    }

    private boolean isInventoryFull(PlayerInventory inventory) {
        boolean hasSpace = false;
        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null) {
                hasSpace = true;
                break;
            }
        }
        return !hasSpace;
    }
}

