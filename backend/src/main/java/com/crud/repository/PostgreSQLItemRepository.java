package com.crud.repository;

import com.crud.exception.ResourceNotFoundException;
import com.crud.model.Item;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLItemRepository implements ItemRepository {
  private final String dbUrl;
  private final String dbUser;
  private final String dbPassword;

  public PostgreSQLItemRepository() {
    String dbHost = System.getenv("DB_HOST");
    String dbPort = System.getenv("DB_PORT");
    String dbName = System.getenv("DB_NAME");

    if (dbHost == null) {
      throw new IllegalStateException("DB_HOST environment variable not set");
    }

    this.dbUrl = System.getenv("DB_URL") != null
        ? System.getenv("DB_URL")
        : "jdbc:postgresql://" + dbHost + ":" + (dbPort != null ? dbPort : "5432") + "/"
            + (dbName != null ? dbName : "cruddb");
    this.dbUser = System.getenv().getOrDefault("DB_USER", "cruduser");
    this.dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "crudpass");

    waitForDatabase();
    initializeTable();
  }

  private void waitForDatabase() {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("PostgreSQL driver not found", e);
    }

    int maxRetries = 30;
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        System.out.println("Attempting to connect to: " + dbUrl);
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        conn.close();
        System.out.println("Database connected successfully!");
        return;
      } catch (SQLException e) {
        retryCount++;
        System.out.println("Waiting for database... (" + retryCount + "/" + maxRetries + ") - " + e.getMessage());
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
    throw new RuntimeException("Failed to connect to database after " + maxRetries + " attempts");
  }

  private void initializeTable() {
    String sql = """
        CREATE TABLE IF NOT EXISTS items (
            id SERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            category VARCHAR(100),
            price DOUBLE PRECISION,
            quantity INTEGER,
            created_at TIMESTAMP,
            updated_at TIMESTAMP
        )
        """;
    try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialize database", e);
    }
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
  }

  @Override
  public Item save(Item item) {
    if (item.getId() == null || item.getId().isBlank()) {
      String sql = "INSERT INTO items (name, description, category, price, quantity, created_at, updated_at) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
      try (Connection conn = getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, item.getName());
        ps.setString(2, item.getDescription());
        ps.setString(3, item.getCategory());
        ps.setDouble(4, item.getPrice() != null ? item.getPrice() : 0.0);
        ps.setInt(5, item.getQuantity() != null ? item.getQuantity() : 0);
        ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
        ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
          item.setId(rs.getString("id"));
        }
        return item;
      } catch (SQLException e) {
        throw new RuntimeException("Failed to save item", e);
      }
    } else {
      String sql = "UPDATE items SET name=?, description=?, category=?, price=?, quantity=?, updated_at=? WHERE id=?";
      try (Connection conn = getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, item.getName());
        ps.setString(2, item.getDescription());
        ps.setString(3, item.getCategory());
        ps.setDouble(4, item.getPrice() != null ? item.getPrice() : 0.0);
        ps.setInt(5, item.getQuantity() != null ? item.getQuantity() : 0);
        ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(7, item.getId());
        ps.executeUpdate();
        return item;
      } catch (SQLException e) {
        throw new RuntimeException("Failed to update item", e);
      }
    }
  }

  @Override
  public List<Item> findAll() {
    List<Item> items = new ArrayList<>();
    String sql = "SELECT * FROM items ORDER BY id";
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        items.add(mapRowToItem(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to fetch items", e);
    }
    return items;
  }

  @Override
  public Item findById(String id) {
    String sql = "SELECT * FROM items WHERE id = ?";
    try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return mapRowToItem(rs);
      }
      throw new ResourceNotFoundException("Item not found with id: " + id);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to fetch item", e);
    }
  }

  @Override
  public List<Item> findByCategory(String category) {
    List<Item> items = new ArrayList<>();
    String sql = "SELECT * FROM items WHERE LOWER(category) = LOWER(?)";
    try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, category);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        items.add(mapRowToItem(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to fetch items by category", e);
    }
    return items;
  }

  @Override
  public Item update(String id, Item item) {
    findById(id);
    item.setId(id);
    return save(item);
  }

  @Override
  public void delete(String id) {
    findById(id);
    String sql = "DELETE FROM items WHERE id = ?";
    try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to delete item", e);
    }
  }

  @Override
  public boolean exists(String id) {
    String sql = "SELECT 1 FROM items WHERE id = ?";
    try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, id);
      ResultSet rs = ps.executeQuery();
      return rs.next();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check item existence", e);
    }
  }

  @Override
  public int count() {
    String sql = "SELECT COUNT(*) FROM items";
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count items", e);
    }
    return 0;
  }

  @Override
  public void clear() {
    String sql = "DELETE FROM items";
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(sql);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to clear items", e);
    }
  }

  @Override
  public String generateId() {
    return null;
  }

  private Item mapRowToItem(ResultSet rs) throws SQLException {
    Item item = new Item();
    item.setId(rs.getString("id"));
    item.setName(rs.getString("name"));
    item.setDescription(rs.getString("description"));
    item.setCategory(rs.getString("category"));
    item.setPrice(rs.getDouble("price"));
    item.setQuantity(rs.getInt("quantity"));
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      item.setCreatedAt(createdAt.toLocalDateTime());
    }
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    if (updatedAt != null) {
      item.setUpdatedAt(updatedAt.toLocalDateTime());
    }
    return item;
  }
}
