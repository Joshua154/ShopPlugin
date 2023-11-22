package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.uis.offers.SeeOfferedItemsGUI;
import de.joshua.util.database.ShopDataBaseUtil;
import de.joshua.util.dbItems.SellItemDataBase;
import de.joshua.util.item.ItemBuilder;
import de.joshua.util.ui.PageGUI;
import net.kyori.adventure.text.Component;
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

public class ShopGUI extends PageGUI {
    ShopPlugin shopPlugin;
    List<SellItemDataBase> db_items;

    public ShopGUI(ShopPlugin shopPlugin) {
        super(Component.text(ShopPlugin.getConfigString("shop.shop.gui.name")));
        this.shopPlugin = shopPlugin;
        updateItems();
    }

    @Override
    public @NotNull Inventory getInventory() {
        String sell = ShopPlugin.getConfigString("shop.shop.gui.button.sell");
        String offeredItems = ShopPlugin.getConfigString("shop.shop.gui.button.offeredItems");
        String storedItems = ShopPlugin.getConfigString("shop.shop.gui.button.storedItems");

        Inventory inventory = super.getInventory();

//        inventory.setItem(9 * 5 + 2, new ItemBuilder(Material.RED_CONCRETE)
//                .displayName(Component.text("Cancel"))
//                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
//                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "")
//                .build());
        inventory.setItem(9 * 5 + 3, new ItemBuilder(Material.EMERALD)
                .displayName(Component.text(sell))
                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "sell")
                .build());
        inventory.setItem(9 * 5 + 4, new ItemBuilder(Material.BUNDLE)
                .displayName(Component.text(offeredItems))
                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "offers")
                .build());
        inventory.setItem(9 * 5 + 5, new ItemBuilder(Material.CHEST)
                .displayName(Component.text(storedItems))
                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "stored_items")
                .build());
//        inventory.setItem(9 * 5 + 6, new ItemBuilder(Material.RED_CONCRETE)
//                .displayName(Component.text("Cancel"))
//                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
//                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "cancel")
//                .build());

        return inventory;
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

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
            case "sell" -> {
                SellGUI sellGUI = new SellGUI(shopPlugin, player);
                sellGUI.open();
            }
            case "offers" -> {
                SeeOfferedItemsGUI seeOfferedItemsGUI = new SeeOfferedItemsGUI(shopPlugin, player);
                seeOfferedItemsGUI.open();
            }
            case "stored_items" -> {
                StoredItemsGUI storedItemsGUI = new StoredItemsGUI(shopPlugin, player);
                storedItemsGUI.open();
            }
        }
    }

    @Override
    public void onPageSwitch() {
        updateItems();
        updateCachedContent();
    }

    private void updateItems() {
        db_items = ShopDataBaseUtil.getForSellItems(shopPlugin);
    }
}

