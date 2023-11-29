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
import java.util.concurrent.CompletableFuture;

public class ShopGUI extends PageGUI {
    ShopPlugin shopPlugin;
    List<SellItemDataBase> db_items;
    ShopCategory currentCategory = ShopCategory.getFirst();
    int categorySlot = 9 * 5 + 2;
    public ShopGUI(ShopPlugin shopPlugin) {
        super(Component.text(ShopPlugin.getConfigString("shop.shop.gui.name")));
        this.shopPlugin = shopPlugin;
        updateItems();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return setupButtons(super.getInventory());
    }

    private Inventory setupButtons(Inventory inventory) {
        String sell = ShopPlugin.getConfigString("shop.shop.gui.button.sell");
        String offeredItems = ShopPlugin.getConfigString("shop.shop.gui.button.offeredItems");
        String storedItems = ShopPlugin.getConfigString("shop.shop.gui.button.storedItems");

        inventory.setItem(categorySlot, new ItemBuilder(currentCategory.displayItem())
                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "category")
                .build());
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
        inventory.setItem(9 * 5 + 6, new ItemBuilder(Material.COMPASS)
                .displayName(Component.text("In Arbeit"))
                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "search")
                .build());
        return inventory;
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public List<ItemStack> getContent() {
        return currentCategory.parseItems(db_items)
                .stream().map(this::generateItem).toList();
    }

    private ItemStack generateItem(SellItemDataBase sellItemDataBase) {
        return new ItemBuilder(sellItemDataBase.getPreviewItem(getPageGUIKey("item_id")))
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
                if (!clickedItem.getItemMeta().getPersistentDataContainer().has(getPageGUIKey("item_id"), PersistentDataType.INTEGER))
                    return;
                Integer dbID = clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("item_id"), PersistentDataType.INTEGER);
                SellItemDataBase sellItemDataBase = db_items.stream().filter(dbI -> dbI.dbID() == dbID).findFirst().orElse(null);
                BuyGUI buyGUI = new BuyGUI(shopPlugin, sellItemDataBase, player);
                buyGUI.open();
            }
            case "category" -> {
                setNextCategory();
                updateCachedContent();
                refresh();
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

    @Override
    public void refresh() {
        super.refresh();
        setupButtons(super.inventory);
    }

    private void updateItems() {
        CompletableFuture<List<SellItemDataBase>> future = ShopDataBaseUtil.getForSellItems(shopPlugin);
        db_items = future.join();
    }

    private void setNextCategory() {
        ShopCategory next = currentCategory.next();
        currentCategory = next;
        inventory.setItem(categorySlot, new ItemBuilder(next.displayItem())
                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "category")
                .build());
    }
}

