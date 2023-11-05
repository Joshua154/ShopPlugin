package de.joshua.util;

import org.apache.commons.lang3.tuple.Pair;
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

public class ShopDataBaseUtil {
    public static void addNewSellItem(Connection connection, ItemStack itemStack, ItemStack price, String seller_uuid) {
        Map<String, Object> values = new java.util.HashMap<>(Map.of(
                "item", Arrays.toString(itemStack.serializeAsBytes()),
                "seller_uuid", seller_uuid
        ));

        if (price != null)
            values.put("price", Arrays.toString(price.serializeAsBytes()));

        String query = DataBaseUtil.getInsertQuery("sell_items", values);
        DataBaseUtil.executeQuery(connection, query);
    }

    public static List<SellItemDataBase> getForSellItems(Connection connection) {
        String query = DataBaseUtil.getSelectWhereQuery("sell_items", "bought IS 0", "*");
        System.out.println(query);
        Pair<ResultSet, PreparedStatement> temp = DataBaseUtil.executeQuery(connection, query);
        ResultSet resultSet = temp.getLeft();
        PreparedStatement statement = temp.getRight();

        List<SellItemDataBase> sellItemDataBases = new java.util.ArrayList<>();
        while (true) {
            try{
                if (resultSet == null) break;
                if (!resultSet.next()) {
                    statement.close();
                    break;
                }
                if (resultSet.getString("item") == null) continue;
                ItemStack item = ItemStack.deserializeBytes(fromString(resultSet.getString("item")));
                ItemStack price = new ItemStack(Material.AIR);
                if (resultSet.getString("price") != null) {
                    price = ItemStack.deserializeBytes(fromString(resultSet.getString("price")));
                }
                String seller_uuid = resultSet.getString("seller_uuid");
                long created_at = resultSet.getLong("createdOn");
                SellItemDataBase sellItemDataBase = new SellItemDataBase(
                        resultSet.getInt("id"),
                        item,
                        price,
                        UUID.fromString(seller_uuid),
                        parseDateTime(created_at)
                );

                sellItemDataBases.add(sellItemDataBase);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            statement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return sellItemDataBases;
    }

    public static List<StoredItemDataBase> getStoredItems(Connection connection, UUID seller_uuid) {
        String query = DataBaseUtil.getSelectWhereQuery("storedItems", "to_uuid IS '" + seller_uuid + "'", "*");
        Pair<ResultSet, PreparedStatement> temp = DataBaseUtil.executeQuery(connection, query);
        ResultSet resultSet = temp.getLeft();
        PreparedStatement statement = temp.getRight();

        List<StoredItemDataBase> sellItemDataBases = new java.util.ArrayList<>();
        while (true) {
            try{
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
                        parseDateTime(created_at)
                );

                sellItemDataBases.add(storedItemDataBase);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            statement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return sellItemDataBases;
    }

    private static LocalDateTime parseDateTime(Long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), TimeZone.getDefault().toZoneId());
    }

    private static byte[] fromString(String input){
        String[] split = input.replace("[", "").replace("]", "").split(", ");
        byte[] output = new byte[split.length];
        for (int i = 0; i < split.length; i++) {
            output[i] = Byte.parseByte(split[i]);
        }
        return output;
    }

    public static void buyItem(Connection connection, SellItemDataBase item, Player buyer) {
        Map<String, Object> values = new java.util.HashMap<>(Map.of(
                "bought", "'1'",
                "buyer_uuid", "'" + buyer.getUniqueId() + "'"
        ));

        String query = DataBaseUtil.getUpdateQuery("sell_items", Map.entry("id", item.dbID()), values);
        DataBaseUtil.executeQuery(connection, query);

        addStoredItem(connection, item.price(), item.seller(), buyer.getUniqueId());
    }

    public static void removeItem(Connection connection, SellItemDataBase item) {
        String query = DataBaseUtil.getDeleteQuery("storedItems", "id=" + item.dbID());
        DataBaseUtil.executeQuery(connection, query);
    }

    public static boolean isStillAvailable(Connection connection, int id){
        String query = DataBaseUtil.getSelectWhereQuery("sell_items", "id=" + id, "bought");
        Pair<ResultSet, PreparedStatement> temp = DataBaseUtil.executeQuery(connection, query);
        ResultSet resultSet = temp.getLeft();
        PreparedStatement statement = temp.getRight();

        while (true) {
            try{
                if (resultSet == null) break;
                if (!resultSet.next()) {
                    statement.close();
                    break;
                }
                if (resultSet.getString("bought") == null) return false;
                return resultSet.getString("bought").equals("0");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            statement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void addStoredItem(Connection connection, ItemStack item, UUID from, UUID to) {
        String query = DataBaseUtil.getInsertQuery("storedItems", Map.of(
                "item", Arrays.toString(item.serializeAsBytes()),
                "from_uuid", from.toString(),
                "to_uuid", to.toString()
        ));
        DataBaseUtil.executeQuery(connection, query);
    }

    public static void removeStoredItem(Connection connection, int id) {
        String query = DataBaseUtil.getDeleteQuery("storedItems", "id=" + id);
        DataBaseUtil.executeQuery(connection, query);
    }
}
