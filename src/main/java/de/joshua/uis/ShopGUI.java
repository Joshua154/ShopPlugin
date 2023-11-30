package de.joshua.uis;

import com.destroystokyo.paper.ClientOption;
import de.joshua.ShopPlugin;
import de.joshua.uis.offers.SeeOfferedItemsGUI;
import de.joshua.util.database.ShopDataBaseUtil;
import de.joshua.util.dbItems.SellItemDataBase;
import de.joshua.util.item.ItemBuilder;
import de.joshua.util.ui.PageGUI;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopGUI extends PageGUI {
    private
    ShopPlugin shopPlugin;
    List<SellItemDataBase> db_items;
    ShopCategory currentCategory = ShopCategory.getFirst();
    Pattern searchPattern;
    int categorySlot = 9 * 5 + 2;

    public ShopGUI(ShopPlugin shopPlugin, Player player) {
        super(Component.text(ShopPlugin.getConfigString("shop.shop.gui.name")));
        this.shopPlugin = shopPlugin;
        this.player = player;
        this.searchPattern = Pattern.compile("\\\\*");
        updateItems();
    }

    public ShopGUI(ShopPlugin shopPlugin, Player player, Pattern searchPattern) {
        super(Component.text(ShopPlugin.getConfigString("shop.shop.gui.name")));
        this.shopPlugin = shopPlugin;
        this.searchPattern = searchPattern;
        this.player = player;
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
        System.out.println(player.getClientOption(ClientOption.LOCALE));
        return currentCategory.parseItems(db_items)
                .stream()
                .filter(item ->
                        checkRegex(item.item().getType().name().toLowerCase()) ||
                        checkRegex(item.price().getType().name().toLowerCase()) ||
                        isPlayer(item))
                .map(this::generateItem).toList();
    }

    private boolean isPlayer(SellItemDataBase item) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(item.seller());
        String name = p.getName();
        return name != null && checkRegex(name.toLowerCase());
    }

    private ItemStack generateItem(SellItemDataBase sellItemDataBase) {
        return new ItemBuilder(sellItemDataBase.getPreviewItem(getPageGUIKey("item_id")))
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "purchasable")
                .build();
    }

    @Override
    public void onItemClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        ClickType clickType = event.getClick();
        Player player = (Player) event.getWhoClicked();

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
                ShopCategory newCategory;
                if(clickType.isLeftClick()) newCategory = currentCategory.next();
                else if(clickType.isRightClick()) newCategory = currentCategory.previous();
                else return;

                setNextCategory(newCategory);
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
            case "search" ->{
                if(clickType.isLeftClick()) {
                    handleSearch();
                } else if(clickType.isRightClick()) {
                    player.closeInventory();
                    new ShopGUI(shopPlugin, player).open();
                }
            }
        }
    }

    private void handleSearch() {
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> {
                    String text = stateSnapshot.getText().toLowerCase();
                    Pattern sp;
                    if(text.contains("\\")) {
                        sp = Pattern.compile(text);
                    } else {
                        sp = Pattern.compile("\\\\*" + text);
                    }

                    new ShopGUI(shopPlugin, player, sp).open();
                })
                .onClick((slot, stateSnapshot) -> List.of(AnvilGUI.ResponseAction.close()))
                .text(ShopPlugin.getConfigString("shop.shop.gui.search.default_text"))
                .title(ShopPlugin.getConfigString("shop.shop.gui.search.title"))
                .plugin(shopPlugin)
                .open(player);
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

    private void setNextCategory(ShopCategory newCategory) {
        currentCategory = newCategory;
        inventory.setItem(categorySlot, new ItemBuilder(newCategory.displayItem())
                .persistentData(getPageGUIKey("shop_gui"), PersistentDataType.STRING, "button")
                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "category")
                .build());
    }

    private boolean checkRegex(String input) {
        Matcher matcher = searchPattern.matcher(input);
        return matcher.find();
    }

    public void open(){
        setCachedContent(getContent());
        player.openInventory(getInventory());
    }
}
