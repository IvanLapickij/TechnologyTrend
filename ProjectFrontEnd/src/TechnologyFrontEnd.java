

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TechnologyFrontEnd extends JFrame {

    // UI components for user interaction.
    private JTextField txtGetName;
    private JTextField txtDeleteId;
    private JTextField txtCurrentName;
    private JTextField txtNewName;
    private JTextField txtYear;
    private JTextField txtCost;
    private JTextField txtCategoryId;

    // Table to display product data.
    private JTable productTable;
    private DefaultTableModel tableModel;

    public TechnologyFrontEnd() {
        super("Distributed Systems Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Set overall content pane background.
        getContentPane().setBackground(Color.BLACK);
        getContentPane().setLayout(new BorderLayout());

        // Create and add left control panel.
        JPanel leftPanel = createLeftControlPanel();
        getContentPane().add(leftPanel, BorderLayout.WEST);

        // Create and add right panel containing the table.
        JPanel rightPanel = createRightTablePanel();
        getContentPane().add(rightPanel, BorderLayout.CENTER);

        // Load products from the REST endpoint.
        loadAllProducts();
    }

    /**
     * Creates the left control panel with a black background and white text.
     */
    private JPanel createLeftControlPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 600));

        int SPACING = 10;

        // "Get Product by Name" section.
        JLabel lblGetName = createWhiteLabel("Get Product by Name:");
        txtGetName = new JTextField();
        styleTextField(txtGetName);
        JButton btnGetByName = new JButton("Get");
        styleButton(btnGetByName);
        btnGetByName.addActionListener(this::handleGetByName);

        // "Delete Product by Id" section.
        JLabel lblDeleteId = createWhiteLabel("Delete Product by Id:");
        txtDeleteId = new JTextField();
        styleTextField(txtDeleteId);
        JButton btnDeleteById = new JButton("Delete");
        styleButton(btnDeleteById);
        btnDeleteById.addActionListener(this::handleDeleteById);

        // "Update Product" section.
        JLabel lblCurrentName = createWhiteLabel("Current Name:");
        txtCurrentName = new JTextField();
        styleTextField(txtCurrentName);
        JLabel lblNewName = createWhiteLabel("New Name:");
        txtNewName = new JTextField();
        styleTextField(txtNewName);
        JLabel lblYear = createWhiteLabel("Year:");
        txtYear = new JTextField();
        styleTextField(txtYear);
        JLabel lblCost = createWhiteLabel("Cost:");
        txtCost = new JTextField();
        styleTextField(txtCost);
        JLabel lblCategoryId = createWhiteLabel("Category ID:");
        txtCategoryId = new JTextField();
        styleTextField(txtCategoryId);
        JButton btnPut = new JButton("Update");
        styleButton(btnPut);
        btnPut.addActionListener(this::handlePut);

        // "Add New Product" section.
        JButton btnPost = new JButton("Add New Product");
        styleButton(btnPost);
        btnPost.addActionListener(this::handlePost);

        // Additional (placeholder) buttons.
        JButton btnDeleteAll = new JButton("Delete All");
        styleButton(btnDeleteAll);
        btnDeleteAll.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete All not implemented."));
        JButton btnPrintToExcel = new JButton("Print To Excel");
        styleButton(btnPrintToExcel);
        btnPrintToExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Print To Excel not implemented."));

        // Add all components to the panel with spacing.
        panel.add(lblGetName);
        panel.add(txtGetName);
        panel.add(btnGetByName);
        panel.add(Box.createRigidArea(new Dimension(0, SPACING)));

        panel.add(lblDeleteId);
        panel.add(txtDeleteId);
        panel.add(btnDeleteById);
        panel.add(Box.createRigidArea(new Dimension(0, SPACING)));

        panel.add(lblCurrentName);
        panel.add(txtCurrentName);
        panel.add(lblNewName);
        panel.add(txtNewName);
        panel.add(lblYear);
        panel.add(txtYear);
        panel.add(lblCost);
        panel.add(txtCost);
        panel.add(lblCategoryId);
        panel.add(txtCategoryId);
        panel.add(btnPut);
        panel.add(Box.createRigidArea(new Dimension(0, SPACING)));

        panel.add(btnPost);
        panel.add(Box.createRigidArea(new Dimension(0, SPACING)));

        panel.add(btnDeleteAll);
        panel.add(btnPrintToExcel);

        return panel;
    }

    /**
     * Creates the right panel with a JTable to display products.
     */
    private JPanel createRightTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        // Updated columns include the Category name.
        tableModel = new DefaultTableModel(new Object[][]{},
                new String[]{"ID", "Name", "Type", "Year", "Cost", "Category"});
        productTable = new JTable(tableModel);
        productTable.setBackground(Color.BLACK);
        productTable.setForeground(Color.WHITE);
        productTable.setGridColor(Color.GRAY);

        // Style the table header.
        JTableHeader header = productTable.getTableHeader();
        header.setBackground(Color.BLACK);
        header.setForeground(Color.WHITE);
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.getViewport().setBackground(Color.BLACK);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Styles a JTextField for dark mode.
     */
    private void styleTextField(JTextField textField) {
        textField.setBackground(Color.BLACK);
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    }

    /**
     * Styles a JButton for dark mode.
     */
    private void styleButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    }

    /**
     * Creates a JLabel with white text.
     */
    private JLabel createWhiteLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }

    /**
     * Loads all products from the REST endpoint (GET /products) and populates the table.
     * Assumes the returned JSON includes the category name (see backend instructions).
     */
    private void loadAllProducts() {
        try {
            URL url = new URL("http://localhost:8080/YourApp/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                // Read the JSON response.
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }
                in.close();

                // Clear the table.
                tableModel.setRowCount(0);

                // TODO: Parse the JSON response using a JSON library (e.g., Gson or Jackson)
                // to extract product fields including the Category name.
                // For demonstration purposes, a dummy row is added:
                tableModel.addRow(new Object[]{1, "The Egg", "Power bank", 2025, "19000.00", "Sustainability"});
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to load products. HTTP code: " + responseCode,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading products: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the "Get" button to retrieve products by name.
     */
    private void handleGetByName(ActionEvent e) {
        String name = txtGetName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a name to GET.");
            return;
        }
        try {
            URL url = new URL("http://localhost:8080/YourApp/products/name/" + name);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                tableModel.setRowCount(0);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }
                in.close();

                // TODO: Parse JSON properly. For now, add a dummy row.
                tableModel.addRow(new Object[]{999, name, "Device", 2025, "330.00", "IOT"});
            } else {
                JOptionPane.showMessageDialog(this,
                        "Product(s) not found or error. HTTP code: " + responseCode,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            con.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error fetching product by name: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the "Delete" button to delete a product by ID.
     */
    private void handleDeleteById(ActionEvent e) {
        String idStr = txtDeleteId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter an ID to delete.");
            return;
        }
        try {
            int id = Integer.parseInt(idStr);
            URL url = new URL("http://localhost:8080/YourApp/products/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");

            int responseCode = con.getResponseCode();
            if (responseCode == 204) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully.");
                loadAllProducts();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete product. HTTP code: " + responseCode,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            con.disconnect();
        } catch (NumberFormatException nfex) {
            JOptionPane.showMessageDialog(this, "ID must be an integer.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting product: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the "Update" button to modify a product.
     * This example uses a dummy flow to get a product by name before updating.
     */
    private void handlePut(ActionEvent e) {
        String currName = txtCurrentName.getText().trim();
        String newName = txtNewName.getText().trim();
        String yearStr = txtYear.getText().trim();
        String costStr = txtCost.getText().trim();
        String categoryIdStr = txtCategoryId.getText().trim();

        if (currName.isEmpty() || newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Current and New name are required.");
            return;
        }
        try {
            int year = Integer.parseInt(yearStr);
            int cost = Integer.parseInt(costStr);
            int categoryId = Integer.parseInt(categoryIdStr);

            // Dummy flow: get product by current name (assuming only one result).
            URL getUrl = new URL("http://localhost:8080/YourApp/products/name/" + currName);
            HttpURLConnection getCon = (HttpURLConnection) getUrl.openConnection();
            getCon.setRequestMethod("GET");
            getCon.setRequestProperty("Accept", "application/json");
            int getResponseCode = getCon.getResponseCode();
            if (getResponseCode == 200) {
                // Dummy productId (in a real scenario, parse the JSON to get the ID).
                int productId = 1;
                URL putUrl = new URL("http://localhost:8080/YourApp/products/" + productId);
                HttpURLConnection putCon = (HttpURLConnection) putUrl.openConnection();
                putCon.setRequestMethod("PUT");
                putCon.setRequestProperty("Content-Type", "application/json");
                putCon.setDoOutput(true);
                String jsonBody = String.format(
                        "{\"productid\": %d, \"name\": \"%s\", \"type\": \"???\", \"year\": %d, \"cost\": %d, \"categoryid\": %d}",
                        productId, newName, year, cost, categoryId);
                try (OutputStream os = putCon.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                int putResponseCode = putCon.getResponseCode();
                if (putResponseCode == 200) {
                    JOptionPane.showMessageDialog(this, "Product updated successfully.");
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update product. HTTP code: " + putResponseCode,
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                putCon.disconnect();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not find product with name: " + currName,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            getCon.disconnect();
        } catch (NumberFormatException nfex) {
            JOptionPane.showMessageDialog(this, "Year, Cost, and CategoryID must be integers.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating product: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the "Add New Product" button.
     */
    private void handlePost(ActionEvent e) {
        JOptionPane.showMessageDialog(this, "Post functionality not fully implemented.");
    }

    /**
     * Main method to launch the front end.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TechnologyFrontEnd frame = new TechnologyFrontEnd();
            frame.setVisible(true);
        });
    }
}
