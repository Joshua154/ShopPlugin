//package de.joshua.util.database;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.LinkedList;
//import java.util.Queue;
//
//public class SQLiteQueue {
//    private static final String DATABASE_URL = "jdbc:sqlite:/path/to/your/database.db";
//
//    private Queue<String> operationQueue;
//    private Connection connection;
//
//    public SQLiteQueue() {
//        operationQueue = new LinkedList<>();
//        initializeDatabase();
//    }
//
//    private void initializeDatabase() {
//        try {
//            connection = DriverManager.getConnection(DATABASE_URL);
//            connection.createStatement().executeUpdate(CREATE_TABLE_QUERY);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void enqueueOperation(String data) {
//        operationQueue.offer(data);
//        executeNextOperation();
//    }
//
//    private void executeNextOperation() {
//        if (!operationQueue.isEmpty()) {
//            String data = operationQueue.peek();
//            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_QUERY)) {
//                preparedStatement.setString(1, data);
//                preparedStatement.executeUpdate();
//                operationQueue.poll(); // Remove the operation from the queue after successful execution
//                executeNextOperation(); // Continue with the next operation
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void dequeueOperation() {
//        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_QUERY)) {
//            preparedStatement.executeUpdate();
//            executeNextOperation(); // Continue with the next operation
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//}
