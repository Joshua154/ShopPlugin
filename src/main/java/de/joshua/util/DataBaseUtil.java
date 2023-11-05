package de.joshua.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.slf4j.helpers.FormattingTuple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            query.append(column).append("=").append(values.get(column)).append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(" WHERE ").append(where.getValue()).append("=").append(where.getKey()).append(";");
        return query.toString();
    }

    public static String getDeleteQuery(String tableName, String where) {
        return "DELETE FROM " + tableName + " WHERE " + where + ";";
    }

    public static Pair<ResultSet, PreparedStatement> executeQuery(Connection connection, String query){
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            if(query.toLowerCase().startsWith("select")) {
                ResultSet rs = statement.executeQuery();
                return Pair.of(rs, statement);
            }
            else {
                statement.executeUpdate();
                statement.close();
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error while executing query: " + query);
            e.printStackTrace();
        }
        return Pair.of(null, null);
    }
}
