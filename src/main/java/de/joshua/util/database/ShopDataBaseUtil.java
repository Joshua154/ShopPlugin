package de.joshua.util.database;

import de.joshua.ShopPlugin;
import de.joshua.util.dbItems.OfferItemDataBase;
import de.joshua.util.dbItems.SellItemDataBase;
import de.joshua.util.dbItems.StoredItemDataBase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class ShopDataBaseUtil {
    public static void addNewSellItem(ShopPlugin shopPlugin, ItemStack itemStack, ItemStack price, String seller_uuid) {
        Map<String, Object> values = new java.util.HashMap<>(Map.of(
                "item", Arrays.toString(itemStack.serializeAsBytes()),
                "seller_uuid", seller_uuid
        ));

        if (price != null)
            values.put("price", Arrays.toString(price.serializeAsBytes()));

        String query = DataBaseUtil.getInsertQuery("sellItems", values);
        shopPlugin.getSQLQueue().enqueueOperation(query);
    }

    public static List<SellItemDataBase> getForSellItems(ShopPlugin shopPlugin) {
        String query = DataBaseUtil.getSelectWhereQuery("sellItems", "bought IS 0", "*");
        CompletableFuture<ResultSet> future = shopPlugin.getSQLQueue().enqueueOperation(query);
        AtomicReference<List<SellItemDataBase>> returnList = new AtomicReference<>(new ArrayList<>());
        future.thenAcceptAsync(resultSet -> {
            while (true) {
                try {
                    if (resultSet == null) break;
                    if (!resultSet.next()) {
                        break;
                    }
                    SellItemDataBase sellItemDataBase = parseItem(resultSet);
                    returnList.get().add(sellItemDataBase);//TODO
                } catch (SQLException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Error while getting sell items from database", e);
                }
            }
        });
        return returnList.get();
    }

    public static List<StoredItemDataBase> getStoredItems(ShopPlugin shopPlugin, UUID seller_uuid) {
        String query = DataBaseUtil.getSelectWhereQuery("storedItems", "to_uuid IS '" + seller_uuid + "'", "*");
        CompletableFuture<ResultSet> future = shopPlugin.getSQLQueue().enqueueOperation(query);

        AtomicReference<List<StoredItemDataBase>> returnList = new AtomicReference<>(new ArrayList<>());
        future.thenAcceptAsync(resultSet -> {
            while (true) {
                try {
                    if (resultSet == null) break;
                    ItemStack item = ItemStack.deserializeBytes(fromString(resultSet.getString("item")));
                    UUID from_uuid = UUID.fromString(resultSet.getString("from_uuid"));
                    String created_at = resultSet.getString("timestamp");
                    StoredItemDataBase storedItemDataBase = new StoredItemDataBase(
                            resultSet.getInt("id"),
                            item,
                            seller_uuid,
                            from_uuid,
                            parseDateTime(created_at),
                            resultSet.getInt("soldItemID")
                    );

                    returnList.get().add(storedItemDataBase);
                } catch (SQLException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Error while getting stored items from database", e);
                }
            }
        });
        return returnList.get();
    }

    private static LocalDateTime parseDateTime(String timestamp) {
        return Timestamp.valueOf(timestamp).toLocalDateTime();
    }

    private static byte[] fromString(String input) {
        String[] split = input.replace("[", "").replace("]", "").split(", ");
        byte[] output = new byte[split.length];
        for (int i = 0; i < split.length; i++) {
            output[i] = Byte.parseByte(split[i]);
        }
        return output;
    }

    public static void buyItem(ShopPlugin shopPlugin, SellItemDataBase item, Player buyer) {
        Map<String, Object> values = new java.util.HashMap<>(Map.of(
                "bought", 1,
                "boughtAt", System.currentTimeMillis(),
                "buyer_uuid", buyer.getUniqueId()
        ));
        removeAllOffers(shopPlugin, item.dbID());
        setSellItemToBought(shopPlugin, item.dbID(), values);
        addStoredItem(shopPlugin, item.price(), buyer.getUniqueId(), item.seller(), item.dbID());
    }

    public static void buyItemWithOffer(ShopPlugin shopPlugin, OfferItemDataBase offer) {
        Map<String, Object> values = new java.util.HashMap<>(Map.of(
                "bought", 1,
                "buyer_uuid", offer.offeredBy(),
                "boughtAt", System.currentTimeMillis(),
                "price", Arrays.toString(offer.offer().serializeAsBytes())
        ));


        addStoredItem(shopPlugin, offer.sellItem().item(), offer.seller(), offer.offeredBy(), offer.dbID());
        removeOffer(shopPlugin, offer.dbID());
        setSellItemToBought(shopPlugin, offer.sellItem().dbID(), values);
        removeAllOffers(shopPlugin, offer.sellItem().dbID());
    }

    public static void setSellItemToBought(ShopPlugin shopPlugin, int id, Map<String, Object> values) {
        String query = DataBaseUtil.getUpdateQuery("sellItems", Map.entry("id", id), values);
        shopPlugin.getSQLQueue().enqueueOperation(query); //TODO add this
    }

    public static void removeItem(ShopPlugin shopPlugin, SellItemDataBase item) {
        removeAllOffers(shopPlugin, item.dbID());
        String query = DataBaseUtil.getDeleteQuery("sellItems", "id=" + item.dbID());
        shopPlugin.getSQLQueue().enqueueOperation(query);
    }

    public static boolean isStillAvailable(ShopPlugin shopPlugin, int id) {
        String query = DataBaseUtil.getSelectWhereQuery("sellItems", "id=" + id, "bought");
        CompletableFuture<ResultSet> future = shopPlugin.getSQLQueue().enqueueOperation(query);

        AtomicReference<Boolean> returnBool = new AtomicReference<>(Boolean.FALSE);
        future.thenAcceptAsync(resultSet -> {
            while (true) {
                try {
                    if (resultSet == null) break;
                    if (!resultSet.next()) break;

                    if (resultSet.getString("bought") == null) return;
                    returnBool.set(resultSet.getString("bought").equals("0"));
                    return;
                } catch (SQLException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Error while getting sell items from database", e);
                }
            }
        });
        return returnBool.get();
    }

    public static void addStoredItem(ShopPlugin shopPlugin, ItemStack item, UUID from, UUID to, int soldItemID) {
        String query = DataBaseUtil.getInsertQuery("storedItems", Map.of(
                "item", Arrays.toString(item.serializeAsBytes()),
                "from_uuid", from.toString(),
                "to_uuid", to.toString(),
                "soldItemID", soldItemID
        ));
        shopPlugin.getSQLQueue().enqueueOperation(query);
    }

    public static void removeStoredItem(ShopPlugin shopPlugin, int id) {
        String query = DataBaseUtil.getDeleteQuery("storedItems", "id=" + id);
        shopPlugin.getSQLQueue().enqueueOperation(query);
    }

    public static Optional<SellItemDataBase> getSpecificSellItem(ShopPlugin shopPlugin, int id) {
        String query = DataBaseUtil.getSelectWhereQuery("sellItems", "bought IS 0 AND id IS " + id, "*");
        CompletableFuture<ResultSet> future = shopPlugin.getSQLQueue().enqueueOperation(query);

        AtomicReference<Optional<SellItemDataBase>> returnOptional = new AtomicReference<>(Optional.empty());
        future.thenAcceptAsync(resultSet -> {
            while (true) {
                try {
                    if (resultSet == null) break;
                    if (!resultSet.next()) break;


                    returnOptional.set(Optional.ofNullable(parseItem(resultSet)));
                } catch (SQLException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Error while getting sell item from database", e);
                }
            }
        });
        return returnOptional.get();
    }

    private static SellItemDataBase parseItem(ResultSet resultSet) {
        SellItemDataBase returnItem = null;
        try {
            if (resultSet.getString("item") == null) return null;
            ItemStack item = ItemStack.deserializeBytes(fromString(resultSet.getString("item")));
            ItemStack price = new ItemStack(Material.AIR);
            if (resultSet.getString("price") != null) {
                price = ItemStack.deserializeBytes(fromString(resultSet.getString("price")));
            }
            String seller_uuid = resultSet.getString("seller_uuid");
            String created_at = resultSet.getString("createdOn");
            returnItem = new SellItemDataBase(
                    resultSet.getInt("id"),
                    item,
                    price,
                    UUID.fromString(seller_uuid),
                    parseDateTime(created_at)
            );
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while parsing Item: " + Arrays.toString(e.getStackTrace()));
        }
        return returnItem;
    }

    public static void addOffer(ShopPlugin shopPlugin, Player player, SellItemDataBase item, ItemStack offeredItem) {
        String query = DataBaseUtil.getInsertQuery("itemOffers", Map.of(
                "offeredItem", Arrays.toString(offeredItem.serializeAsBytes()),
                "from_uuid", player.getUniqueId().toString(),
                "to_uuid", item.seller(),
                "buyItemID", item.dbID()
        ));
        shopPlugin.getSQLQueue().enqueueOperation(query);
    }

    public static void removeOffer(ShopPlugin shopPlugin, int offerId) {
        String query = DataBaseUtil.getDeleteQuery("itemOffers", "id=" + offerId);
        shopPlugin.getSQLQueue().enqueueOperation(query);
    }

    public static void removeAllOffers(ShopPlugin shopPlugin, int sellItemID) {
        List<OfferItemDataBase> getOfferedItems = getOfferedItems(shopPlugin, sellItemID);
        getOfferedItems.forEach(offerItemDataBase -> {
            addStoredItem(shopPlugin, offerItemDataBase.offer(), offerItemDataBase.seller(), offerItemDataBase.offeredBy(), -1);
            removeOffer(shopPlugin, offerItemDataBase.dbID());
        });
    }

    public static List<OfferItemDataBase> getOfferedItems(ShopPlugin shopPlugin, UUID seller_uuid) {
        String query = DataBaseUtil.getSelectWhereQuery("itemOffers", "to_uuid IS '" + seller_uuid + "'", "*");
        return getOffersWithQuery(shopPlugin, query);
    }

    public static List<OfferItemDataBase> getOfferedItems(ShopPlugin shopPlugin, int buyItemID) {
        String query = DataBaseUtil.getSelectWhereQuery("itemOffers", "buyItemID IS '" + buyItemID + "'", "*");
        return getOffersWithQuery(shopPlugin, query);
    }

    private static List<OfferItemDataBase> getOffersWithQuery(ShopPlugin shopPlugin, String query) {
        CompletableFuture<ResultSet> future = shopPlugin.getSQLQueue().enqueueOperation(query);

        AtomicReference<List<OfferItemDataBase>> returnList = new AtomicReference<>(new ArrayList<>());
        future.thenAcceptAsync(resultSet -> {
            List<SellItemDataBase> cachedItems = new java.util.ArrayList<>();
            while (true) {
                try {
                    if (resultSet == null) break;
                    if (!resultSet.next()) break;

                    ItemStack offeredItem = ItemStack.deserializeBytes(fromString(resultSet.getString("offeredItem")));
                    UUID from_uuid = UUID.fromString(resultSet.getString("from_uuid"));
                    UUID seller_uuid = UUID.fromString(resultSet.getString("to_uuid"));
                    String created_at = resultSet.getString("timestamp");
                    int id = resultSet.getInt("id");
                    int buyItemID = resultSet.getInt("buyItemID");
                    SellItemDataBase sellItem = null;
                    if (cachedItems.isEmpty() || cachedItems.stream().noneMatch(sellItemDataBase -> sellItemDataBase.dbID() == id)) {
                        Optional<SellItemDataBase> sellItemDataBase = getSpecificSellItem(shopPlugin, buyItemID);
                        if (sellItemDataBase.isPresent()) {
                            sellItem = sellItemDataBase.get();
                            cachedItems.add(sellItemDataBase.get());
                        }
                    } else {
                        sellItem = cachedItems.stream().filter(sellItemDataBase -> sellItemDataBase.dbID() == id).findFirst().orElse(null);
                    }
                    if (sellItem == null) continue;

                    OfferItemDataBase offerItemDataBase = new OfferItemDataBase(
                            id,
                            seller_uuid,
                            from_uuid,
                            offeredItem,
                            parseDateTime(created_at),
                            sellItem
                    );

                    returnList.get().add(offerItemDataBase);
                } catch (SQLException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Error while getting stored items from database", e);
                }
            }
        });
        return returnList.get();
    }
}
