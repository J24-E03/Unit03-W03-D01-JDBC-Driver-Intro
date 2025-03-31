package com.dci;

import de.vandermeer.asciitable.AsciiTable;

import java.sql.*;

public class Main {
    private static final String url = "jdbc:postgresql://localhost:5432/library_db";
    private static final String username = "test";
    private static final String password = "test";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("connection successful");

            // Task 2: Insert New Books
            insertBook(connection, "Long Lost", "Jacqueline West", 2012, true);
            insertBook(connection, "Get Inked", "Anna Banks", 2011, false);
            insertBook(connection, "Young Gotham", "Marie Lu", 2015, true);
            insertBook(connection, "The John Green", "John Green", 2003, false);
            insertBook(connection, "A Sarah Dessen", "Jeyn Roberts", 2009, true);

            // Task 3: Retrieve All Books
            getAllBooks(connection);

            // Task 4: Find Books Published Between Two Years
            findBooksBetweenYears(connection, 2010, 2016);

            // Task 5: Retrieve Available Books
            getAvailableBooks(connection);

            //BONUS CHALLENGES
            // 1. Paginate book results
            paginateBooks(connection, 2, 2);

            //BONUS CHALLENGES
            // 2. Find books by title prefix
            findBooksByTitlePrefix(connection, "A");

            //BONUS CHALLENGES
            // 3. Sort books dynamically
            getBooksSorted(connection, "author", "desc");

            //BONUS CHALLENGES
            // 4. Advanced Search
            findBooksByTitleAuthorYear(connection, "ee", "oh", 2000, 2005);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertBook(Connection connection, String title, String author, int publicationYear, boolean isAvailable) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into books(title, author, publication_year, is_available) values (?, ?, ?, ?)")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            preparedStatement.setInt(3, publicationYear);
            preparedStatement.setBoolean(4, isAvailable);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getAllBooks(Connection connection) {
        try (ResultSet resultSet = connection.createStatement().executeQuery("select * from books")) {
            printTable(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void findBooksBetweenYears(Connection connection, int startYear, int endYear) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from books where publication_year between ? and ? order by publication_year")) {
            preparedStatement.setInt(1, startYear);
            preparedStatement.setInt(2, endYear);
            printTable(preparedStatement.executeQuery());
        }
    }

    private static void getAvailableBooks(Connection connection) {
        try (ResultSet resultSet = connection.createStatement().executeQuery("select * from books where is_available is true")) {
            printTable(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void paginateBooks(Connection connection, int limit, int offset) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from books offset ? limit ?")) {
            preparedStatement.setInt(1, limit);
            preparedStatement.setInt(2, offset);
            printTable(preparedStatement.executeQuery());
        }
    }

    private static void findBooksByTitlePrefix(Connection connection, String prefix) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from books where title like ?")) {
            preparedStatement.setString(1, prefix + "%");
            printTable(preparedStatement.executeQuery());
        }
    }

    private static void getBooksSorted(Connection connection, String orderBy, String orderDirection) throws SQLException {
        String sql = "select * from books order by " + orderBy + " " + orderDirection;
        printTable(connection.createStatement().executeQuery(sql));
    }

    private static void findBooksByTitleAuthorYear(Connection connection, String titleSubstring, String authorSubstring, int startYear, int endYear) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from books where title like ? and author like ? and publication_year between ? and ?")) {
            preparedStatement.setString(1, "%" + titleSubstring + "%");
            preparedStatement.setString(2, "%" + authorSubstring + "%");
            preparedStatement.setInt(3, startYear);
            preparedStatement.setInt(4, endYear);
            printTable(preparedStatement.executeQuery());
        }
    }

    private static void printTable(ResultSet resultSet) throws SQLException {
        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("ID", "Title", "Author", "Year", "Available");
        table.addRule();

        while (resultSet.next()) {
            table.addRow(resultSet.getInt("book_id"),
                    resultSet.getString("title"),
                    resultSet.getString("author"),
                    resultSet.getInt("publication_year"),
                    resultSet.getBoolean("is_available"));
            table.addRule();
        }
        System.out.println(table.render());
    }
}
