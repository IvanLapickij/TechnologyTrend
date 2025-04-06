package project.rest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public enum ProductDAO {
    INSTANCE;

    private Connection connection;

    // Initialize the database connection
    ProductDAO() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/oneDB", "SA", "Passw0rd");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Retrieve all products, including their associated company information
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.ProductID, p.Name, p.Type, p.Year, p.Cost, p.CategoryName, p.CompanyID, " +
                       "c.CompanyName, c.Years " +
                       "FROM Products p LEFT JOIN Companies c ON p.CompanyID = c.CompanyID";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Product product = new Product();
                product.setProductid(rs.getInt("ProductID"));
                product.setName(rs.getString("Name"));
                product.setType(rs.getString("Type"));
                product.setYear(rs.getInt("Year"));
                product.setCost(rs.getDouble("Cost"));
                product.setCategoryName(rs.getString("CategoryName"));

                Company company = new Company();
                company.setCompanyID(rs.getInt("CompanyID"));
                company.setCompanyName(rs.getString("CompanyName"));
                company.setYears(rs.getInt("Years"));
                product.setCompany(company);

                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    // Retrieve a product by its ID
    public Product getProductById(int id) {
        String query = "SELECT p.ProductID, p.Name, p.Type, p.Year, p.Cost, p.CategoryName, p.CompanyID, " +
                       "c.CompanyName, c.Years " +
                       "FROM Products p LEFT JOIN Companies c ON p.CompanyID = c.CompanyID " +
                       "WHERE p.ProductID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductid(rs.getInt("ProductID"));
                    product.setName(rs.getString("Name"));
                    product.setType(rs.getString("Type"));
                    product.setYear(rs.getInt("Year"));
                    product.setCost(rs.getDouble("Cost"));
                    product.setCategoryName(rs.getString("CategoryName"));

                    Company company = new Company();
                    company.setCompanyID(rs.getInt("CompanyID"));
                    company.setCompanyName(rs.getString("CompanyName"));
                    company.setYears(rs.getInt("Years"));
                    product.setCompany(company);

                    return product;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Retrieve products by name
    public List<Product> getProductByName(String name) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.ProductID, p.Name, p.Type, p.Year, p.Cost, p.CategoryName, p.CompanyID, " +
                       "c.CompanyName, c.Years " +
                       "FROM Products p LEFT JOIN Companies c ON p.CompanyID = c.CompanyID " +
                       "WHERE p.Name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductid(rs.getInt("ProductID"));
                    product.setName(rs.getString("Name"));
                    product.setType(rs.getString("Type"));
                    product.setYear(rs.getInt("Year"));
                    product.setCost(rs.getDouble("Cost"));
                    product.setCategoryName(rs.getString("CategoryName"));

                    Company company = new Company();
                    company.setCompanyID(rs.getInt("CompanyID"));
                    company.setCompanyName(rs.getString("CompanyName"));
                    company.setYears(rs.getInt("Years"));
                    product.setCompany(company);

                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }
    
    public int getNextAvailableId() {
        String query = "SELECT ProductID FROM Products ORDER BY ProductID";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            int expected = 1;
            while (rs.next()) {
                int current = rs.getInt("ProductID");
                if (current != expected) {
                    return expected; // found a gap
                }
                expected++;
            }
            return expected; // no gaps found, return next after highest
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // error
    }


    // Insert a new product
    public void addProduct(Product product) {
        String query = "INSERT INTO Products (ProductID, Name, Type, Year, Cost, CompanyID, CategoryName) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, product.getProductid());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getType());
            stmt.setInt(4, product.getYear());
            stmt.setDouble(5, product.getCost());
            stmt.setInt(6, product.getCompany().getCompanyID());
            stmt.setString(7, product.getCategoryName());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update an existing product
    public void updateProduct(Product product) {
        String query = "UPDATE Products SET Name = ?, Type = ?, Year = ?, Cost = ?, CompanyID = ?, CategoryName = ? " +
                       "WHERE ProductID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setInt(3, product.getYear());
            stmt.setDouble(4, product.getCost());
            stmt.setInt(5, product.getCompany().getCompanyID());
            stmt.setString(6, product.getCategoryName());
            stmt.setInt(7, product.getProductid());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a product by ID
    public void deleteProduct(int id) {
        String query = "DELETE FROM Products WHERE ProductID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteAll() {
        String query = "DELETE FROM Products";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
