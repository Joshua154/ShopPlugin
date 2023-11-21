package de.joshua.uis.offers;

import de.joshua.ShopPlugin;
import de.joshua.util.database.ShopDataBaseUtil;
import de.joshua.util.ShopUtil;
import de.joshua.util.dbItems.SellItemDataBase;
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

public class MakeOfferGUI implements IGUI {
    private final Player player;
    private final SellItemDataBase item;
    private final Integer priceSlot = 18 + 2;
    ShopPlugin shopPlugin;
    private Inventory inventory;

    public MakeOfferGUI(ShopPlugin shopPlugin, SellItemDataBase item, Player player) {
        this.shopPlugin = shopPlugin;
        this.player = player;
        this.item = item;
    }

    protected NamespacedKey getGUIKey(String key) {
        return new NamespacedKey("joshua.gui.item", key);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        if (!clickedItem.hasItemMeta()) {
            event.setCancelled(false);
            return;
        }
        if (!clickedItem.getItemMeta().getPersistentDataContainer().has(getGUIKey("make_offer_gui"), PersistentDataType.STRING))
            return;
        if (Objects.requireNonNull(clickedItem.getItemMeta().getPersistentDataContainer().get(getGUIKey("make_offer_gui"), PersistentDataType.STRING)).equals("button")) {
            if (!clickedItem.getItemMeta().getPersistentDataContainer().has(getGUIKey("type"), PersistentDataType.STRING))
                return;
            switch (Objects.requireNonNull(clickedItem.getItemMeta().getPersistentDataContainer().get(getGUIKey("type"), PersistentDataType.STRING))) {
                case "confirm" -> offerPrice(inventory.getItem(priceSlot));
                case "cancel" -> player.closeInventory();
            }
        }
    }

    private void offerPrice(ItemStack offeredItem) {
        if (item.isNotAvailable(shopPlugin.getDatabaseConnection())) {
            String msg = ShopPlugin.getConfigString("shop.error.itemSold");
            ShopPlugin.sendMessage(Component.text(msg), player);
            player.closeInventory();
            return;
        }
        boolean validOffer = offeredItem != null && offeredItem.getType() != Material.AIR;
        offeredItem = validOffer ? offeredItem.clone() : null;

        inventory.setItem(priceSlot, new ItemBuilder().build());
        player.closeInventory();
        if (validOffer) {
            ShopDataBaseUtil.addOffer(shopPlugin.getDatabaseConnection(), player, item, offeredItem);
            String msg = ShopPlugin.getConfigString("shop.makeOffer.success");
            ShopPlugin.sendMessage(Component.text(msg), player);
        } else {
            String msg = ShopPlugin.getConfigString("shop.makeOffer.invalid");
            ShopPlugin.sendMessage(Component.text(msg), player);
        }
    }

    @Override
    public void open(Player player) {
        this.inventory = getInventory();
        player.openInventory(this.inventory);
    }

    public void open() {
        this.open(this.player);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        if (inventory.getItem(priceSlot) != null && Objects.requireNonNull(inventory.getItem(priceSlot)).getType() != Material.AIR) {
            ShopUtil.addItemToInventory(player, inventory.getItem(priceSlot));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        String title = ShopPlugin.getConfigString("shop.makeOffer.gui.name");
        String buyItem = ShopPlugin.getConfigString("shop.makeOffer.gui.display.buyItem");
        String offeredItem = ShopPlugin.getConfigString("shop.makeOffer.gui.display.offeredItem");
        String confirm = ShopPlugin.getConfigString("shop.makeOffer.gui.button.confirm");
        String cancel = ShopPlugin.getConfigString("shop.makeOffer.gui.button.cancel");


        Inventory inventory = Bukkit.createInventory(this, 9 * 4, Component.text(title));

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                    .displayName(Component.text(" "))
                    .persistentData(getGUIKey("make_offer_gui"), PersistentDataType.STRING, "background")
                    .build());
        }

        inventory.setItem(9 + 1, new ItemBuilder(Material.PAPER)
                .displayName(Component.text(buyItem))
                .persistentData(getGUIKey("make_offer_gui"), PersistentDataType.STRING, "property")
                .build());
        inventory.setItem(9 + 2, new ItemBuilder(item.item())
                .persistentData(getGUIKey("make_offer_gui"), PersistentDataType.STRING, "sell_item")
                .build());

        inventory.setItem(9 + 9 + 1, new ItemBuilder(Material.PAPER)
                .displayName(Component.text(offeredItem))
                .persistentData(getGUIKey("make_offer_gui"), PersistentDataType.STRING, "property")
                .build());
        inventory.setItem(9 + 9 + 2, new ItemBuilder().build());


        inventory.setItem(9 + 9 + 6, new ItemBuilder(Material.LIME_CONCRETE)
                .displayName(Component.text(confirm))
                .persistentData(getGUIKey("make_offer_gui"), PersistentDataType.STRING, "button")
                .persistentData(getGUIKey("type"), PersistentDataType.STRING, "confirm")
                .build());
        inventory.setItem(9 + 9 + 7, new ItemBuilder(Material.RED_CONCRETE)
                .displayName(Component.text(cancel))
                .persistentData(getGUIKey("make_offer_gui"), PersistentDataType.STRING, "button")
                .persistentData(getGUIKey("type"), PersistentDataType.STRING, "cancel")
                .build());

        return inventory;
    }
}