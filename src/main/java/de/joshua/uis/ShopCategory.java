//package de.joshua.uis;
//
//import de.joshua.util.item.ItemBuilder;
//import org.bukkit.Material;
//import org.bukkit.inventory.CreativeCategory;
//import org.bukkit.inventory.ItemStack;
//
//public enum ShopCategory {
//    BUILDING_BLOCKS(new ItemBuilder(Material.GRASS_BLOCK).build(), .BUILDING_BLOCKS),
//    DECORATIONS(new ItemBuilder(Material.GRASS_BLOCK).build(), CreativeCategory.DECORATIONS),
//    REDSTONE(new ItemBuilder(Material.GRASS_BLOCK).build(), CreativeCategory.REDSTONE),
//    TRANSPORTATION(new ItemBuilder(Material.GRASS_BLOCK).build(), CreativeCategory.TRANSPORTATION),
//    MISC(new ItemBuilder(Material.GRASS_BLOCK).build(), CreativeCategory.MISC),
//    FOOD(new ItemBuilder(Material.GRASS_BLOCK).build(), CreativeCategory.FOOD),
//    TOOLS(new ItemBuilder(Material.GRASS_BLOCK).build(), CreativeCategory.TOOLS),
//    COMBAT(new ItemBuilder(Material.GRASS_BLOCK).build(), CreativeCategory.COMBAT),
//    BREWING(new ItemBuilder(Material.GRASS_BLOCK).build(), CreativeCategory.BREWING),
//    ALL(new ItemBuilder(Material.GRASS_BLOCK).build(), null);
//
//
//    private final ItemStack displayItem;
//    private final CreativeCategory creativeCategory;
//
//    ShopCategory(ItemStack displayItem, CreativeCategory creativeCategory) {
//        this.displayItem = displayItem;
//        this.creativeCategory = creativeCategory;
//    }
//
//    public ItemStack getDisplayItem() {
//        return displayItem;
//    }
//
//    public boolean isInCategory(ItemStack itemStack) {
//        if(this == ALL) return true;
//        return itemStack.getType().getCreativeCategory() == this.creativeCategory;
//    }
//}
