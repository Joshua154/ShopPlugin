package de.joshua.util;

import de.joshua.util.dbItems.OfferItemDataBase;
import de.joshua.util.dbItems.SellItemDataBase;
import de.joshua.util.dbItems.StoredItemDataBase;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class ShopDataBaseUtil {
    public static void addNewSellItem(Connection connection, ItemStack itemStack, ItemStack price, String seller_uuid) {
        Map<String, Object> values = new java.util.HashMap<>(Map.of(
                "item", Arrays.toString(itemStack.serializeAsBytes()),
                "seller_uuid", seller_uuid
        ));

        if (price != null)
            values.put("price", Arrays.toString(price.serializeAsBytes()));

        String query = DataBaseUtil.getInsertQuery("sellItems", values);
        DataBaseUtil.executeQuery(connection, query);
    }

    public static List<SellItemDataBase> getForSellItems(Connection connection) {
        String query = DataBaseUtil.getSelectWhereQuery("sellItems", "bought IS 0", "*");
        Pair<ResultSet, PreparedStatement> temp = DataBaseUtil.executeQuery(connection, query);
        ResultSet resultSet = temp.getLeft();
        PreparedStatement statement = temp.getRight();

        List<SellItemDataBase> sellItemDataBases = new java.util.ArrayList<>();
        while (true) {
            try {
                if (resultSet == null) break;
                if (!resultSet.next()) {
                    if(statement != null)
                        statement.close();
                    break;
                }
                SellItemDataBase sellItemDataBase = parseItem(resultSet);
                if (sellItemDataBase != null)
                    sellItemDataBases.add(sellItemDataBase);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.WARNING, "Error while getting sell items from database", e);
            }
        }
        try {
            if(statement != null)
                statement.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error while getting sell items from database", e);
        }
        return sellItemDataBases;
    }

    public static List<StoredItemDataBase> getStoredItems(Connection connection, UUID seller_uuid) {
        String query = DataBaseUtil.getSelectWhereQuery("storedItems", "to_uuid IS '" + seller_uuid + "'", "*");
        Pair<ResultSet, PreparedStatement> temp = DataBaseUtil.executeQuery(connection, query);
        ResultSet resultSet = temp.getLeft();
        PreparedStatement statement = temp.getRight();

        List<StoredItemDataBase> storedItemDataBases = new java.util.ArrayList<>();
        while (true) {
            try {
                if (resultSet == null) break;
                if (!resultSet.next()) {
                    statement.close();
                    break;
                }
                ItemStack item = ItemStack.deserializeBytes(fromString(resultSet.getString("item")));
                UUID from_uuid = UUID.fromString(resultSet.getString("from_uuid"));
                long created_at = resultSet.getLong("timestamp");
                StoredItemDataBase storedItemDataBase = new StoredItemDataBase(
                        resultSet.getInt("id"),
                        item,
                        seller_uuid,
                        from_uuid,
                        parseDateTime(created_at),
                        resultSet.getInt("soldItemID")
                );

                storedItemDataBases.add(storedItemDataBase);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.WARNING, "Error while getting stored items from database", e);
            }
        }
        try {
            statement.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error while getting sell items from database", e);
        }
        return storedItemDataBases;
    }

    private static LocalDateTime parseDateTime(Long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), TimeZone.getDefault().toZoneId());
    }

    private static byte[] fromString(String input) {
        String[] split = input.replace("[", "").replace("]", "").split(", ");
        byte[] output = new byte[split.length];
        for (int i = 0; i < split.length; i++) {
            output[i] = Byte.parseByte(split[i]);
        }
        return output;
    }

    public static void buyItem(Connection connection, SellItemDataBase item, Player buyer) {
        Map<String, Object> values = new java.util.HashMap<>(Map.of(
                "bought", 1,
                "boughtAt", System.currentTimeMillis(),
                "buyer_uuid", buyer.getUniqueId()
        ));
        removeAllOffers(connection, item.dbID());
        setSellItemToBought(connection, item.dbID(), values);
        addStoredItem(connection, item.price(), buyer.getUniqueId(), item.seller(), item.dbID());
    }

    public static void buyItemWithOffer(Connection connection, OfferItemDataBase offer) {
        Map<String, Object> values = new java.util.HashMap<>(Map.of(
                "bought", 1,
                "buyer_uuid", offer.offeredBy(),
                "boughtAt", System.currentTimeMillis(),
                "price", Arrays.toString(offer.offer().serializeAsBytes())
        ));


        addStoredItem(connection, offer.sellItem().item(), offer.offeredBy(), offer.seller(), offer.dbID());
        removeOffer(connection, offer.dbID());
        setSellItemToBought(connection, offer.sellItem().dbID(), values);
        removeAllOffers(connection, offer.sellItem().dbID());
    }

    public static void setSellItemToBought(Connection connection, int id, Map<String, Object> values) {
        String query = DataBaseUtil.getUpdateQuery("sellItems", Map.entry("id", id), values);
        DataBaseUtil.executeQuery(connection, query); //TODO add this
    }

    public static void removeItem(Connection connection, SellItemDataBase item) {
        String query = DataBaseUtil.getDeleteQuery("storedItems", "id=" + item.dbID());
        DataBaseUtil.executeQuery(connection, query);
        removeAllOffers(connection, item.dbID());
    }

    public static boolean isStillAvailable(Connection connection, int id) {
        String query = DataBaseUtil.getSelectWhereQuery("sellItems", "id=" + id, "bought");
        Pair<ResultSet, PreparedStatement> temp = DataBaseUtil.executeQuery(connection, query);
        ResultSet resultSet = temp.getLeft();
        PreparedStatement statement = temp.getRight();

        while (true) {
            try {
                if (resultSet == null) break;
                if (!resultSet.next()) {
                    statement.close();
                    break;
                }
                if (resultSet.getString("bought") == null) return false;
                return resultSet.getString("bought").equals("0");
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.WARNING, "Error while getting sell items from database", e);
            }
        }
        try {
            statement.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error while getting sell items from database", e);
        }
        return false;
    }

    public static void addStoredItem(Connection connection, ItemStack item, UUID from, UUID to, int soldItemID) {
        String query = DataBaseUtil.getInsertQuery("storedItems", Map.of(
                "item", Arrays.toString(item.serializeAsBytes()),
                "from_uuid", from.toString(),
                "to_uuid", to.toString(),
                "soldItemID", soldItemID
        ));
        DataBaseUtil.executeQuery(connection, query);
    }

    public static void removeStoredItem(Connection connection, int id) {
        String query = DataBaseUtil.getDeleteQuery("storedItems", "id=" + id);
        DataBaseUtil.executeQuery(connection, query);
    }

    public static Optional<SellItemDataBase> getSpecificSellItem(Connection connection, int id) {
        String query = DataBaseUtil.getSelectWhereQuery("sellItems", "bought IS 0 AND id IS " + id, "*");
        Pair<ResultSet, PreparedStatement> temp = DataBaseUtil.executeQuery(connection, query);
        ResultSet resultSet = temp.getLeft();
        PreparedStatement statement = temp.getRight();

        Optional<SellItemDataBase> sellItemDataBase = Optional.empty();
        while (true) {
            try {
                if (resultSet == null) break;
                if (!resultSet.next()) {
                    statement.close();
                    break;
                }


                sellItemDataBase = Optional.ofNullable(parseItem(resultSet));
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.WARNING, "Error while getting sell item from database", e);
            }
        }
        try {
            statement.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error while getting sell item from database", e);
        }
        return sellItemDataBase;
    }

    private static SellItemDataBase parseItem(ResultSet resultSet){
        SellItemDataBase returnItem = null;
        try {
            if (resultSet.getString("item") == null) return null;
            ItemStack item = ItemStack.deserializeBytes(fromString(resultSet.getString("item")));
            ItemStack price = new ItemStack(Material.AIR);
            if (resultSet.getString("price") != null) {
                price = ItemStack.deserializeBytes(fromString(resultSet.getString("price")));
            }
            String seller_uuid = resultSet.getString("seller_uuid");
            long created_at = resultSet.getLong("createdOn");
            returnItem = new SellItemDataBase(
                    resultSet.getInt("id"),
                    item,
                    price,
                    UUID.fromString(seller_uuid),
                    parseDateTime(created_at)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnItem;
    }

    public static void addOffer(Connection connection, Player player, SellItemDataBase item, ItemStack offeredItem) {
        String query = DataBaseUtil.getInsertQuery("itemOffers", Map.of(
                "offeredItem", Arrays.toString(offeredItem.serializeAsBytes()),
                "from_uuid", player.getUniqueId().toString(),
                "to_uuid", item.seller(),
                "buyItemID", item.dbID()
        ));
        DataBaseUtil.executeQuery(connection, query);
    }

    public static void removeOffer(Connection connection, int offerId) {
        String query = DataBaseUtil.getDeleteQuery("itemOffers", "id=" + offerId);
        DataBaseUtil.executeQuery(connection, query);
    }

    public static void removeAllOffers(Connection connection, int sellItemID) {
        List<OfferItemDataBase> getOfferedItems = getOfferedItems(connection, sellItemID);
        getOfferedItems.forEach(offerItemDataBase -> {
            System.out.println(offerItemDataBase.dbID());
            addStoredItem(connection, offerItemDataBase.offer(), offerItemDataBase.seller(), offerItemDataBase.offeredBy(), -1);
            removeOffer(connection, offerItemDataBase.dbID());
        });
    }

    public static List<OfferItemDataBase> getOfferedItems(Connection connection, UUID seller_uuid) {
        String query = DataBaseUtil.getSelectWhereQuery("itemOffers", "to_uuid IS '" + seller_uuid + "'", "*");
        return getOffersWithQuery(connection, query);
    }

    public static List<OfferItemDataBase> getOfferedItems(Connection connection, int buyItemID) {
        String query = DataBaseUtil.getSelectWhereQuery("itemOffers", "buyItemID IS '" + buyItemID + "'", "*");
        return getOffersWithQuery(connection, query);
    }

    private static List<OfferItemDataBase> getOffersWithQuery(Connection connection, String query){
        Pair<ResultSet, PreparedStatement> temp = DataBaseUtil.executeQuery(connection, query);
        ResultSet resultSet = temp.getLeft();
        PreparedStatement statement = temp.getRight();

        List<OfferItemDataBase> offeredItems = new java.util.ArrayList<>();
        List<SellItemDataBase> cachedItems = new java.util.ArrayList<>();
        while (true) {
            try {
                if (resultSet == null) break;
                if (!resultSet.next()) {
                    statement.close();
                    break;
                }
                ItemStack offeredItem = ItemStack.deserializeBytes(fromString(resultSet.getString("offeredItem")));
                UUID from_uuid = UUID.fromString(resultSet.getString("from_uuid"));
                UUID seller_uuid = UUID.fromString(resultSet.getString("to_uuid"));
                long created_at = resultSet.getLong("timestamp");
                int id = resultSet.getInt("id");
                int buyItemID = resultSet.getInt("buyItemID");
                SellItemDataBase sellItem = null;
                if(cachedItems.isEmpty() || cachedItems.stream().noneMatch(sellItemDataBase -> sellItemDataBase.dbID() == id)) {
                    Optional<SellItemDataBase> sellItemDataBase = getSpecificSellItem(connection, buyItemID);
                    if (sellItemDataBase.isPresent()) {
                        sellItem = sellItemDataBase.get();
                        cachedItems.add(sellItemDataBase.get());
                    }
                }else {
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

                offeredItems.add(offerItemDataBase);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.WARNING, "Error while getting stored items from database", e);
            }
        }
        try {
            statement.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Error while getting sell items from database", e);
        }
        return offeredItems;
    }
}
