package de.joshua.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public record DataBaseCollection(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet){
    public void close() {
        closeConnection();
        closePreparedStatement();
        closeResultSet();
    }

    public void closeConnection() {
        try {
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closePreparedStatement() {
        try {
            if (preparedStatement != null) preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeResultSet() {
        try {
            if (resultSet != null) resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
