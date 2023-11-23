package de.joshua.util.database;

import de.joshua.ShopPlugin;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class DataBaseUtil {
    public static String getCreateTableQuery(String tableName, String... columns) {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (String column : columns) {
            query.append(column).append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(");");
        return query.toString();
    }

    public static String getInsertQuery(String tableName, Map<String, Object> values) {
        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String column : values.keySet()) {
            query.append(column).append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(") VALUES (");
        for (Object value : values.values()) {
            query.append("'").append(value).append("', ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(");");
        return query.toString();
    }

    public static String getSelectQuery(String tableName, String... columns) {
        StringBuilder query = new StringBuilder("SELECT ");
        for (String column : columns) {
            query.append(column).append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(" FROM ").append(tableName).append(";");
        return query.toString();
    }

    public static String getSelectWhereQuery(String tableName, String where, String... columns) {
        StringBuilder query = new StringBuilder("SELECT ");
        for (String column : columns) {
            query.append(column).append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(" FROM ").append(tableName).append(" WHERE ").append(where).append(";");
        return query.toString();
    }

    public static String getUpdateQuery(String tableName, Map.Entry<String, Object> where, Map<String, Object> values) {
        StringBuilder query = new StringBuilder("UPDATE " + tableName + " SET ");
        for (String column : values.keySet()) {
            query.append(column).append("=").append("'").append(values.get(column)).append("'").append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(" WHERE ").append(where.getKey()).append("=").append("'").append(where.getValue()).append("'").append(";");
        return query.toString();
    }

    public static String getDeleteQuery(String tableName, String where) {
        return "DELETE FROM " + tableName + " WHERE " + where + ";";
    }


    public static CompletableFuture<ResultSet> executeQuery(Connection connection, String query) {
        return CompletableFuture.supplyAsync(() -> {
            Connection conn = null;
            PreparedStatement ps;
            ResultSet rs = null;

            try{
                ps = connection.prepareStatement(query);
                if (query.toLowerCase().startsWith("select")) {
                    rs = ps.executeQuery();
                } else {
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                ShopPlugin.getDiscordWebhook().sendError(Map.of("Query", query, "Message", e.getMessage()));
                Bukkit.getLogger().warning("Error while executing query: " + query + "\n" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).reduce("", (s1, s2) -> s1 + s2 + "\n"));
                e.printStackTrace();
            }
            return rs;
        });
    }
}
