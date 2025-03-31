package org.dci;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String url = "jdbc:postgresql://localhost:5432/library_db";
    private static final String username = "postgres";
    private static final String password = "123456";

    public static void main(String[] args) {
        try (Connection connection = getConnection();) {
            task2(connection);
            task3(connection);
            task4(connection);
            task5(connection);
            bonusTask1(connection);
            bonusTask2(connection);
            bonusTask3(connection);
            bonusTask4(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Task 1:
    private static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    // Task 2
    private static void task2(Connection connection) {
        System.out.println("\nTask 2: Insert New Books");
        insertNewBooks(connection, "The Catcher in the Rye", "J.D. Salinger", 1951, true);
        insertNewBooks(connection, "1984", "George Orwell", 1949, true);
        insertNewBooks(connection, "To Kill a Mockingbird", "Harper Lee", 1960, true);

    }

    private static void insertNewBooks(Connection connection, String title, String author,
                                       int publicationYear, boolean isAvailable) {
        if (bookExists(connection, title, author)) {
            System.out.println("Book already exists: " + title + " by " + author);
            return;
        }

        String query = "INSERT INTO books (title, author, publication_year, is_available) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            preparedStatement.setInt(3, publicationYear);
            preparedStatement.setBoolean(4, isAvailable);

            int rowsAdded = preparedStatement.executeUpdate();
            if (rowsAdded > 0) {
                System.out.println("Book \"" + title + "\" Inserted");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean bookExists(Connection connection, String title, String author) {
        String query = "SELECT COUNT(*) FROM books WHERE title = ? AND author = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private static Book createNewBook(ResultSet resultSet) throws SQLException {
        return new Book (
                resultSet.getInt("book_id"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getInt("publication_year"),
                resultSet.getBoolean("is_available"));

    }

    // Task 3
    private static void task3(Connection connection) {
        System.out.println("\nTask 3: Retrieve All Books");
        List<Book> books = getAllBooks(connection);
        books.forEach(System.out::println);
    }

    private static List<Book> getAllBooks(Connection connection) {
        List<Book> books = new ArrayList<>();

        String query = "SELECT * FROM books ORDER BY title";

        try (Statement statement = connection.createStatement();  ResultSet resultSet = statement.executeQuery(query);) {
            while (resultSet.next()) {
                books.add(createNewBook(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return books;
    }

    // Task 4
    private static void task4(Connection connection) {
        System.out.println("\nTask 4: Find Books Published Between Two Years");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter starting year: ");
        int startYear = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter ending year: ");
        int endYear = scanner.nextInt();
        scanner.nextLine();
        List<Book> books = findBooksBetweenYears(connection, startYear, endYear);
        books.forEach(System.out::println);
    }

    private static List<Book> findBooksBetweenYears(Connection connection, int startYear, int endYear) {
        List<Book> books = new ArrayList<>();

        String query = "SELECT * FROM books WHERE publication_year >= ? AND publication_year <= ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setInt(1, startYear);
            preparedStatement.setInt(2, endYear);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                books.add(createNewBook(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

    // Task 5
    private static void task5(Connection connection) {
        System.out.println("\nTask 5: Retrieve Available Books");
        List<Book> books = getAvailableBooks(connection);
        books.forEach(System.out::println);
    }

    private static List<Book> getAvailableBooks(Connection connection) {
        List<Book> books = new ArrayList<>();

        String query = "SELECT * FROM books WHERE is_available IS NOT NULL";

        try (Statement statement = connection.createStatement();  ResultSet resultSet = statement.executeQuery(query);) {
            while (resultSet.next()) {
                books.add(createNewBook(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return books;
    }

    // Bonus Challenges
    // 1. Bonus Challenges
    private static void bonusTask1(Connection connection) {
        System.out.println("\nBonus Task 1: Paginate book results");
        List<Book> books = paginateBooks(connection, 2, 0);
        books.forEach(System.out::println);
    }

    private static List<Book> paginateBooks(Connection connection, int limit, int offset) {
        String query = "SELECT * FROM books LIMIT ? OFFSET ?";

        List<Book> books = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setInt(1,limit);
            preparedStatement.setInt(2, offset);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                books.add(createNewBook(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

    // 2. Find books by title prefix:
    private static void bonusTask2(Connection connection) {
        System.out.println("\nBonus Task 2: Find books by title prefix");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter title prefix:");
        String prefix = scanner.nextLine();
        List<Book> books = findBooksByTitlePrefix(connection, prefix);
        books.forEach(System.out::println);

    }

    private static List<Book> findBooksByTitlePrefix(Connection connection, String prefix) {
         List<Book> books = new ArrayList<>();
         String query = "SELECT * FROM books WHERE title LIKE ?";

         try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
             preparedStatement.setString(1, prefix + "%");
             ResultSet resultSet = preparedStatement.executeQuery();
             while (resultSet.next()) {
                 books.add(createNewBook(resultSet));
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }

         return books;
    }

    // 3. Sort books dynamically
    private static void bonusTask3(Connection connection) {
        System.out.println("\nBonus Task 3: Sort books dynamically:");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter column to sort by (title, author, publication_year, is_available):");
        String orderBy = scanner.nextLine();
        System.out.println("Enter order direction (ASC/DESC):");
        String orderDirection = scanner.nextLine();

        try {
            List<Book> books = getBooksSorted(connection, orderBy, orderDirection);
            books.forEach(System.out::println);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private static List<Book> getBooksSorted(Connection connection, String orderBy, String orderDirection) {
        List<Book> books = new ArrayList<>();
        List<String> validColumns = List.of("title", "author", "publication_year", "is_available");
        List<String> validDirections = List.of("ASC", "DESC");

        if (!validColumns.contains(orderBy) || !validDirections.contains(orderDirection)) {
            throw new IllegalArgumentException("Invalid sorting parameters");
        }

        String query = "SELECT * FROM books ORDER BY " + orderBy + " " + orderDirection;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                books.add(createNewBook(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return books;
    }

    // 4. Advanced Search
    private static void bonusTask4(Connection connection) {
        System.out.println("\nBonus Task 4: Advanced Search");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter title keyword:");
        String titleSubstring = scanner.nextLine();
        System.out.println("Enter author keyword:");
        String authorSubstring = scanner.nextLine();
        System.out.println("Enter start year:");
        int startYear = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter end year:");
        int endYear = scanner.nextInt();
        scanner.nextLine();
        List<Book> books = findBooksByTitleAuthorYear(connection, titleSubstring, authorSubstring, startYear, endYear);
        books.forEach(System.out::println);
    }

    private static List<Book> findBooksByTitleAuthorYear(Connection connection, String titleSubstring,
                                                         String authorSubstring, int startYear, int endYear) {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE title ILIKE ? AND author ILIKE ? AND publication_year BETWEEN ? AND ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, "%" + titleSubstring + "%");
            preparedStatement.setString(2, "%" + authorSubstring + "%");
            preparedStatement.setInt(3, startYear);
            preparedStatement.setInt(4, endYear);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                books.add(createNewBook(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }
}

