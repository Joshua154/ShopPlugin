package de.joshua.util.database;

import de.joshua.ShopPlugin;
import net.kyori.adventure.text.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public record DataBaseCollection(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
    public void close() {
        closeConnection();
        closePreparedStatement();
        closeResultSet();
    }

    public void closeConnection() {
        try {
            if (connection != null) connection.close();
        } catch (Exception e) {
            ShopPlugin.sendMessageToAdmin(ShopPlugin.getPrefix().append(Component.text("Error while closing connection: " + e.getMessage())));
        }
    }

    public void closePreparedStatement() {
        try {
            if (preparedStatement != null) preparedStatement.close();
        } catch (Exception e) {
            ShopPlugin.sendMessageToAdmin(ShopPlugin.getPrefix().append(Component.text("Error while closing connection: " + e.getMessage())));
        }
    }

    public void closeResultSet() {
        try {
            if (resultSet != null) resultSet.close();
        } catch (Exception e) {
            ShopPlugin.sendMessageToAdmin(ShopPlugin.getPrefix().append(Component.text("Error while closing connection: " + e.getMessage())));
        }
    }
}
