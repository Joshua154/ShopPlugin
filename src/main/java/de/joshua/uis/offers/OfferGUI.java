package de.joshua.uis.offers;

import de.joshua.ShopPlugin;
import de.joshua.util.database.ShopDataBaseUtil;
import de.joshua.util.ShopUtil;
import de.joshua.util.dbItems.OfferItemDataBase;
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
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OfferGUI implements IGUI {
    private final Player player;
    private final OfferItemDataBase offer;
    ShopPlugin shopPlugin;

    public OfferGUI(ShopPlugin shopPlugin, OfferItemDataBase offer, Player player) {
        this.shopPlugin = shopPlugin;
        this.player = player;
        this.offer = offer;
    }

    protected NamespacedKey getGUIKey(String key) {
        return new NamespacedKey("joshua.gui.item", key);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getCurrentItem() == null) return;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        if (!clickedItem.hasItemMeta()) return;
        if (!clickedItem.getItemMeta().getPersistentDataContainer().has(getGUIKey("buy_gui"), PersistentDataType.STRING))
            return;
        if (Objects.requireNonNull(clickedItem.getItemMeta().getPersistentDataContainer().get(getGUIKey("buy_gui"), PersistentDataType.STRING)).equals("button")) {
            if (!clickedItem.getItemMeta().getPersistentDataContainer().has(getGUIKey("type"), PersistentDataType.STRING))
                return;
            switch (Objects.requireNonNull(clickedItem.getItemMeta().getPersistentDataContainer().get(getGUIKey("type"), PersistentDataType.STRING))) {
                case "confirm" -> handleConfirmOffer();
                case "deny" -> handleDenyOffer();
                case "cancel" -> player.closeInventory();
            }
        }
    }

    private void handleDenyOffer() {
        ShopPlugin.sendMessage(Component.text("Offer Removed"), player);

        ShopDataBaseUtil.removeOffer(shopPlugin.getDatabaseConnection(), offer.dbID());
        ShopDataBaseUtil.addStoredItem(shopPlugin.getDatabaseConnection(), offer.offer(), offer.seller(), offer.offeredBy(), -1);

        player.closeInventory();
    }

    private void handleConfirmOffer() {
        if (offer.sellItem().isNotAvailable(shopPlugin.getDatabaseConnection())) {
            String msg = ShopPlugin.getConfigString("shop.error.itemSold");
            ShopPlugin.sendMessage(Component.text(msg), player);
            player.closeInventory();
            return;
        }

        String msg = ShopPlugin.getConfigString("shop.offers.accept");
        ShopPlugin.sendMessage(Component.text(msg), player);
        ShopUtil.addItemToInventory(player, offer.offer());
        player.closeInventory();
        ShopDataBaseUtil.buyItemWithOffer(shopPlugin.getDatabaseConnection(), offer);
    }

    @Override
    public void open(Player player) {
        Inventory inventory = getInventory();
        player.openInventory(inventory);
    }

    public void open() {
        this.open(this.player);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public @NotNull Inventory getInventory() {
        String title = ShopPlugin.getConfigString("shop.offers.gui.name");
        String buyItem = ShopPlugin.getConfigString("shop.offers.gui.display.buyItem");
        String offeredItem = ShopPlugin.getConfigString("shop.offers.gui.display.offeredItem");
        String confirm = ShopPlugin.getConfigString("shop.offers.gui.button.confirm");
        String cancel = ShopPlugin.getConfigString("shop.offers.gui.button.cancel");
        String deny = ShopPlugin.getConfigString("shop.offers.gui.button.deny");


        Inventory inventory = Bukkit.createInventory(this, 9 * 4, Component.text(title));

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                    .displayName(Component.text(" "))
                    .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "background")
                    .build());
        }

        inventory.setItem(9 + 1, new ItemBuilder(Material.PAPER)
                .displayName(Component.text(buyItem))
                .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "property")
                .build());
        inventory.setItem(9 + 2, offer.sellItem().item());

        inventory.setItem(9 + 9 + 1, new ItemBuilder(Material.PAPER)
                .displayName(Component.text(offeredItem))
                .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "property")
                .build());
        inventory.setItem(9 + 9 + 2, offer.offer());


        if (!offer.offer().getType().equals(Material.AIR)) {
            inventory.setItem(9 + 9 + 6, new ItemBuilder(Material.LIME_CONCRETE)
                    .displayName(Component.text(confirm))
                    .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                    .persistentData(getGUIKey("type"), PersistentDataType.STRING, "confirm")
                    .build());
        }
        inventory.setItem(9 + 9 + 7, new ItemBuilder(Material.RED_CONCRETE)
                .displayName(Component.text(cancel))
                .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                .persistentData(getGUIKey("type"), PersistentDataType.STRING, "cancel")
                .build());
        if (this.player.getUniqueId().equals(offer.seller())) {
            inventory.setItem(9 + 9 + 8, new ItemBuilder(Material.BARRIER)
                    .displayName(Component.text(deny))
                    .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                    .persistentData(getGUIKey("type"), PersistentDataType.STRING, "deny")
                    .build());
        }

        return inventory;
    }
}