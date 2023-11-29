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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuantiySelection extends PageGUI {
    ShopPlugin shopPlugin;
    SellGUI sellGUI;

    public QuantiySelection(ShopPlugin shopPlugin, Player player, SellGUI gui) {
        super(Component.text(ShopPlugin.getConfigString("shop.selection.quantity.gui.display.buyItem")));
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
        List<Integer> quantities = new ArrayList<>();
        for (int i = 1; i <= 64 * 4; i++) {
            quantities.add(i);
        }

        return quantities.stream()
                .map(n ->
                        new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                                .amount(n)
                                .displayName(Component.text(n.toString()))
                                .persistentData(getPageGUIKey("type"), PersistentDataType.STRING, "quantity_selection")
                                .persistentData(getPageGUIKey("item_quantity"), PersistentDataType.INTEGER, n)
                                .build()
                ).toList();
    }

    @Override
    public void onItemClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;
        if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(getPageGUIKey("type"), PersistentDataType.STRING))
            return;
        if (!Objects.equals(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(getPageGUIKey("type"), PersistentDataType.STRING), "quantity_selection"))
            return;

        sellGUI.setQuantity(getItemQuantity(event.getCurrentItem()));
        sellGUI.open();
    }

    @Override
    public void onPageSwitch() {

    }

    private Integer getItemQuantity(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer().get(getPageGUIKey("item_quantity"), PersistentDataType.INTEGER);
    }
}
