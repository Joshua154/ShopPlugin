package de.joshua.util.ui;

import de.joshua.util.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class PageGUI implements IGUI {
    protected Component guiTitle;
    protected int itemsPerPage;
    protected int page;
    protected Player player;

    public PageGUI(Component guiTitle) {
        this.guiTitle = guiTitle;
        this.page = 0;
        this.itemsPerPage = 9 * 5;
    }

    protected NamespacedKey getPageGUIKey(String key) {
        return new NamespacedKey("joshua.pagegui.item", key);
    }

    protected int getPage() {
        return page;
    }

    protected int getPageCount() {
        return Math.max(1, (int) Math.ceil(getContent().size() / Math.max(itemsPerPage, 1.0)));
    }

    protected void switchPage(boolean direction) {
        // direction: false -> back, true -> further
        if (getPageCount() == 1) return;
        if (direction) page += 1;
        else page -= 1;
        if (page < 0) page = 0;
        if (page >= getPageCount()) page = getPageCount() - 1;
        onPageSwitch();
        refresh();
    }

    protected List<ItemStack> getItemsFromPage(int page) {
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min((page + 1) * itemsPerPage, getContent().size());

        if (getContent().isEmpty()) return List.of();
        return getContent().subList(startIndex, endIndex);
    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, itemsPerPage + 9, guiTitle);
        List<ItemStack> itemsOnPage = getItemsFromPage(this.page);
        for (int i = 0; i < itemsPerPage + 1; i++) {
            if (i < itemsOnPage.size()) {
                inventory.setItem(i, itemsOnPage.get(i));
            }
        }
        for (int i = this.itemsPerPage; i < this.itemsPerPage + 9; i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                    .displayName(Component.empty())
                    .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "background")
                    .build());
        }
        if (getPage() != 0) {
            inventory.setItem(5 * 9 + 1, new ItemBuilder(Material.ARROW)
                    .displayName(Component.text("Back"))
                    .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "back")
                    .build());
        }
        if (getPage() + 1 < getPageCount()) {
            inventory.setItem(5 * 9 + 7, new ItemBuilder(Material.ARROW)
                    .displayName(Component.text("Further"))
                    .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "further")
                    .build());
        }
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;
        ItemMeta itemMeta = clickedItem.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (container.has(getPageGUIKey("type"))) {
            switch (Objects.requireNonNull(container.get(getPageGUIKey("type"), PersistentDataType.STRING))) {
                case "back":
                    switchPage(false);
                    return;
                case "further":
                    switchPage(true);
                    return;
            }
        }
        onItemClick(event);
    }

    @Override
    public void open(Player player) {
        player.openInventory(getInventory());
        this.player = player;
    }

    public void open() {
        this.open(player);
    }

    public void refresh() {
        player.openInventory(getInventory());
    }

    public abstract List<ItemStack> getContent();

    public abstract void onItemClick(InventoryClickEvent event);

    public abstract void onPageSwitch();
}