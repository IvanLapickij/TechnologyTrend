package project.rest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public enum ProductDAO {
    INSTANCE;
    
    private Connection connection;
    
    // In the constructor, load the driver and open the connection.
    ProductDAO() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/oneDB", "SA", "Passw0rd");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Product product = new Product();
                product.setProductid(rs.getInt("productid"));
                product.setName(rs.getString("name"));
                product.setType(rs.getString("type"));
                product.setYear(rs.getInt("year"));
                product.setCost(rs.getInt("cost"));
                product.setCategoryid(rs.getInt("categoryid"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    public Product getProductById(int id) {
        String query = "SELECT * FROM products WHERE productid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Product product = new Product();
                product.setProductid(rs.getInt("productid"));
                product.setName(rs.getString("name"));
                product.setType(rs.getString("type"));
                product.setYear(rs.getInt("year"));
                product.setCost(rs.getInt("cost"));
                product.setCategoryid(rs.getInt("categoryid"));
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void addProduct(Product product) {
        String query = "INSERT INTO products (name, type, year, cost, categoryid) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setInt(3, product.getYear());
            stmt.setInt(4, product.getCost());
            stmt.setInt(5, product.getCategoryid());
            stmt.executeUpdate();
            
            // Optionally, set the generated productid back to the product instance.
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                product.setProductid(keys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    public void updateProduct(Product product) {
        String query = "UPDATE products SET name = ?, type = ?, year = ?, cost = ?, categoryid = ? WHERE productid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setInt(3, product.getYear());
            stmt.setInt(4, product.getCost());
            stmt.setInt(5, product.getCategoryid());
            stmt.setInt(6, product.getProductid());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteProduct(int id) {
        String query = "DELETE FROM products WHERE productid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
