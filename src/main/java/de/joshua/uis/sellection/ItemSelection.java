package de.joshua.uis.sellection;

import de.joshua.ShopPlugin;
import de.joshua.uis.SellGUI;
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ItemSelection extends PageGUI {
    ShopPlugin shopPlugin;
    private final String title = "Select Item";
    SellGUI sellGUI;

    public ItemSelection(ShopPlugin shopPlugin, Player player, SellGUI gui) {
        super(Component.text("Select Item"));
        super.player = player;
        this.shopPlugin = shopPlugin;
        this.sellGUI = gui;
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
        sellGUI.open();
    }

    @Override
    public List<ItemStack> getContent() {
        return getMaterials().stream()
                .map(m ->
                        new ItemBuilder(m)
                                .displayName(Component.text(toCamelCase(m.name().replaceAll("_", " "))))
                                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "item_selection")
                                .persistentData(getPageGUIKey("item_name"), PersistentDataType.STRING, m.name())
                                .build()
                ).toList();
    }

    private List<Material> getMaterials() {
        return Arrays.stream(Material.values())
                .filter(m -> m.isItem() && !m.isAir())
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    @Override
    public void onItemClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;
        if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(getPageGUIKey("type"), PersistentDataType.STRING))
            return;
        if (!Objects.equals(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(getPageGUIKey("type"), PersistentDataType.STRING), "item_selection"))
            return;

        sellGUI.setMaterial(event.getCurrentItem().getType());
        new QuantiySelection(shopPlugin, player, sellGUI).open();
    }

    @Override
    public void onPageSwitch() {
        List<ItemStack> itemsFromPage = getItemsFromPage(getPage());
        guiTitle = Component.text(title + ": " + getItemName(itemsFromPage.get(0)).toUpperCase().charAt(0) + " - " + getItemName(itemsFromPage.get(itemsFromPage.size() - 1)).toUpperCase().charAt(0));
    }

    private String toCamelCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String getItemName(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("item_name"), PersistentDataType.STRING);
    }
}
