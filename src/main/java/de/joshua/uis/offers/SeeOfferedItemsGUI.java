package de.joshua.uis.offers;

import de.joshua.ShopPlugin;
import de.joshua.uis.ShopGUI;
import de.joshua.util.database.ShopDataBaseUtil;
import de.joshua.util.dbItems.OfferItemDataBase;
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

public class SeeOfferedItemsGUI extends PageGUI {
    ShopPlugin shopPlugin;
    List<OfferItemDataBase> db_items;

    public SeeOfferedItemsGUI(ShopPlugin shopPlugin, Player player) {
        super(Component.text(ShopPlugin.getConfigString("shop.offeredItems.gui.name")));
        super.player = player;
        this.shopPlugin = shopPlugin;
        updateItems();
    }


    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public List<ItemStack> getContent() {
        return db_items.stream().map(this::generateItem).toList();
    }

    private ItemStack generateItem(OfferItemDataBase offerItemDataBase) {
        return new ItemBuilder(offerItemDataBase.getPreviewItem())
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "offered_item")
                .persistentData(getPageGUIKey("id"), PersistentDataType.INTEGER, offerItemDataBase.dbID())
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
        if (!Objects.equals(clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("type"), PersistentDataType.STRING), "offered_item"))
            return;

        int id = Objects.requireNonNull(clickedItem.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("id"), PersistentDataType.INTEGER));
        OfferItemDataBase offeredItem = db_items.stream().filter(item -> item.dbID() == id).findFirst().orElse(null);
        if (offeredItem == null) return;

        new OfferGUI(shopPlugin, offeredItem, player).open();
    }

    private void handleBack() {
        new ShopGUI(shopPlugin, player).open();
    }

    @Override
    public void onPageSwitch() {
        updateItems();
        updateCachedContent();
    }

    private void updateItems() {
        CompletableFuture<List<OfferItemDataBase>> future = ShopDataBaseUtil.getOfferedItems(shopPlugin, this.player.getUniqueId());
        db_items = future.join();
    }

    @Override
    public @NotNull Inventory getInventory() {
        String goBack = ShopPlugin.getConfigString("shop.offeredItems.gui.button.back");

        Inventory inventory = super.getInventory();
        inventory.setItem(9 * 5 + 4, new ItemBuilder(Material.RED_CONCRETE)
                .displayName(Component.text(goBack))
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "go_back_gui")
                .build());
        return inventory;
    }
}