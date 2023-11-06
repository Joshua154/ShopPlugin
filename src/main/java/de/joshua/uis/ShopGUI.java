package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.util.ShopDataBaseUtil;
import de.joshua.util.dbItems.SellItemDataBase;
import de.joshua.util.item.ItemBuilder;
import de.joshua.util.ui.PageGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ShopGUI extends PageGUI {
    ShopPlugin shopPlugin;
    List<SellItemDataBase> db_items;

    public ShopGUI(ShopPlugin shopPlugin) {
        super(Component.text("Shop"));
        this.shopPlugin = shopPlugin;
        updateItems();
    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory = super.getInventory();
        //TODO: Add categories
        return inventory;
    }

    @Override
    public void onClose(Player player, Inventory inventory) {

    }

    @Override
    public List<ItemStack> getContent() {
        return db_items.stream().map(this::generateItem).toList();
    }

    private ItemStack generateItem(SellItemDataBase sellItemDataBase) {
        return new ItemBuilder(sellItemDataBase.getPreviewItem())
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "purchasable")
                .build();
    }

    @Override
    public void onItemClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (clickedItem == null) return;
        if (!clickedItem.hasItemMeta()) return;
        if (!clickedItem.getItemMeta().getPersistentDataContainer().has(getPageGUIKey("type"), PersistentDataType.STRING))
            return;
        switch (Objects.requireNonNull(clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("type"), PersistentDataType.STRING))) {
            case "purchasable" -> {
                SellItemDataBase sellItemDataBase = db_items.get(slot);
                BuyGUI buyGUI = new BuyGUI(shopPlugin, sellItemDataBase, player);
                buyGUI.open();
            }
            case "category" -> {

            }
        }
    }

    @Override
    public void onPageSwitch() {
        updateItems();
    }

    private void updateItems() {
        db_items = ShopDataBaseUtil.getForSellItems(shopPlugin.getDatabaseConnection());
    }
}

