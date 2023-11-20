package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.commands.AnnouceCommand;
import de.joshua.uis.offers.MakeOfferGUI;
import de.joshua.util.ShopDataBaseUtil;
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

import java.util.List;
import java.util.Objects;

public class BuyGUI implements IGUI {
    private final Player player;
    private final SellItemDataBase item;
    ShopPlugin shopPlugin;

    public BuyGUI(ShopPlugin shopPlugin, SellItemDataBase item, Player player) {
        this.shopPlugin = shopPlugin;
        this.player = player;
        this.item = item;
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
                case "confirm" -> makePurchase();
                case "offer" -> handlePriceOffer();
                case "cancel" -> handleCancel();
                case "remove" -> handleRemove();
                case "annouce" -> handleAnnounce();
            }
        }
    }

    private void handleCancel() {
        new ShopGUI(shopPlugin).open(player);
    }

    private void handleAnnounce() {
        AnnouceCommand.sendAnnouncement(String.valueOf(this.item.dbID()), this.player.displayName());
    }

    private void makePurchase() {
        if (item.isNotAvailable(shopPlugin.getDatabaseConnection())) {
            ShopPlugin.sendMessage(Component.text("This item is no longer available"), player);
            player.closeInventory();
            return;
        }
        boolean hasBought = false;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;
            ItemStack editedItemStack = itemStack.clone();
            ItemStack editedPriceItemStack = item.price();

            if (isShulkerBox(editedPriceItemStack) && isShulkerBox(editedItemStack)) {
                editedPriceItemStack.setType(editedItemStack.getType());
            }
            if (editedItemStack.isSimilar(editedPriceItemStack)) {
                if (itemStack.getAmount() < item.price().getAmount()) continue;
                itemStack.setAmount(itemStack.getAmount() - item.price().getAmount());
                hasBought = true;
                break;
            }
        }
        if (hasBought) {
            ShopPlugin.sendMessage(Component.text("Item bought"), player);

            ShopUtil.addItemToInventory(player, item.item());

            player.closeInventory();
            ShopDataBaseUtil.buyItem(shopPlugin.getDatabaseConnection(), item, player);
            return;
        } else {
            ShopPlugin.sendMessage(Component.text("You can't afford this item"), player);
        }

        player.closeInventory();
    }

    private void handlePriceOffer() {
        new MakeOfferGUI(shopPlugin, item, player).open();
    }

    private boolean isShulkerBox(ItemStack itemStack) {
        List<Material> shulkerBoxes = List.of(Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX,
                Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
                Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
                Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
                Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
                Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX);
        return shulkerBoxes.contains(itemStack.getType());
    }

    private void handleRemove() {
        if (item.isNotAvailable(shopPlugin.getDatabaseConnection())) {
            ShopPlugin.sendMessage(Component.text("This item is already sold"), player);
            player.closeInventory();
            return;
        }

        ShopPlugin.sendMessage(Component.text("Item Removed"), player);

        ShopDataBaseUtil.removeItem(shopPlugin.getDatabaseConnection(), item);
        ShopUtil.addItemToInventory(player, item.item());

        player.closeInventory();
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
        Inventory inventory = Bukkit.createInventory(this, 9 * 4, Component.text("Buy"));

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                    .displayName(Component.text(" "))
                    .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "background")
                    .build());
        }

        inventory.setItem(9 + 1, new ItemBuilder(Material.PAPER)
                .displayName(Component.text("Item to Buy"))
                .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "property")
                .build());
        inventory.setItem(9 + 2, item.item());

        inventory.setItem(9 + 9 + 1, new ItemBuilder(Material.PAPER)
                .displayName(Component.text("Price"))
                .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "property")
                .build());
        inventory.setItem(9 + 9 + 2, item.price());


        if (!item.price().getType().equals(Material.AIR)) {
            inventory.setItem(9 + 9 + 5, new ItemBuilder(Material.LIME_CONCRETE)
                    .displayName(Component.text("Confirm Purchase"))
                    .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                    .persistentData(getGUIKey("type"), PersistentDataType.STRING, "confirm")
                    .build());
        }
        inventory.setItem(9 + 9 + 6, new ItemBuilder(Material.ORANGE_CONCRETE)
                .displayName(Component.text("Send Price Offer"))
                .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                .persistentData(getGUIKey("type"), PersistentDataType.STRING, "offer")
                .build());
        inventory.setItem(9 + 9 + 7, new ItemBuilder(Material.RED_CONCRETE)
                .displayName(Component.text("Cancel"))
                .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                .persistentData(getGUIKey("type"), PersistentDataType.STRING, "cancel")
                .build());
        if (this.player.getUniqueId().equals(item.seller())) {
            inventory.setItem(9 + 9 + 8, new ItemBuilder(Material.BARRIER)
                    .displayName(Component.text("Remove"))
                    .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                    .persistentData(getGUIKey("type"), PersistentDataType.STRING, "remove")
                    .build());
        }
        if (this.player.getUniqueId().equals(item.seller()) && this.player.hasPermission("shopplugin.announce")) {
            inventory.setItem(9 + 8, new ItemBuilder(Material.GOAT_HORN)
                    .displayName(Component.text("Annouce"))
                    .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                    .persistentData(getGUIKey("type"), PersistentDataType.STRING, "annouce")
                    .build());
        }

        return inventory;
    }
}