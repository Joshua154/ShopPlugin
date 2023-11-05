package de.joshua.uis;

import de.joshua.ShopPlugin;
import de.joshua.util.SellItemDataBase;
import de.joshua.util.ShopDataBaseUtil;
import de.joshua.util.item.ItemBuilder;
import de.joshua.util.ui.IGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class BuyGUI implements IGUI {
    ShopPlugin shopPlugin;
    private Player player;
    private SellItemDataBase item;
    private Inventory inventory;

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
                case "cancel" -> player.closeInventory();
                case "remove" -> handleRemove();
            }
        }
    }

    private void makePurchase() {
        if (!item.isStillAvailable(shopPlugin.getDatabaseConnection())) {
            player.sendMessage("This item is no longer available");
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
            player.sendMessage(Component.text("Item bought"));

            if (isInventoryFull(player.getInventory())) {
                player.getWorld().dropItem(player.getLocation(), item.item());
            } else {
                player.getInventory().addItem(item.item());
            }

            player.closeInventory();
            //ShopDataBaseUtil.buyItem(shopPlugin.getDatabaseConnection(), item, player); //TODO add this
            return;
        } else {
            player.sendMessage(Component.text("You can't afford this item"));
        }

        player.closeInventory();
    }

    private void handlePriceOffer() {

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
        if (!item.isStillAvailable(shopPlugin.getDatabaseConnection())) {
            player.sendMessage("This item is already sold");
            player.closeInventory();
            return;
        }

        player.sendMessage(Component.text("Item Removed"));

        ShopDataBaseUtil.removeItem(shopPlugin.getDatabaseConnection(), item);
        if (isInventoryFull(player.getInventory())) {
            player.getWorld().dropItem(player.getLocation(), item.item());
        } else {
            player.getInventory().addItem(item.item());
        }

        player.closeInventory();
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
    public void onClose(Player player, Inventory inventory) {

    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, 9 * 4, Component.text("Sell"));

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
        System.out.println(this.player.getUniqueId() + " " + item.seller());
        if (this.player.getUniqueId().equals(item.seller())) {
            inventory.setItem(9 + 9 + 8, new ItemBuilder(Material.BARRIER)
                    .displayName(Component.text("Remove"))
                    .persistentData(getGUIKey("buy_gui"), PersistentDataType.STRING, "button")
                    .persistentData(getGUIKey("type"), PersistentDataType.STRING, "remove")
                    .build());
        }

        return inventory;
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