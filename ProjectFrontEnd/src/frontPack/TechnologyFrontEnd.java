package frontPack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TechnologyFrontEnd extends JFrame {

    private JTextField txtGetName, txtDeleteId, txtName, txtType, txtYear, txtCost, txtCategory, txtCompanyId, txtUpdateId;
    private JTable productTable;
    private DefaultTableModel tableModel;

    // Button color definitions
    private final Color Blue = new Color(70, 130, 180);
    private final Color Red = new Color(220, 20, 60);
    private final Color Green = new Color(34, 139, 34);
    private final Color Orange = new Color(255, 140, 0);
    private final Color colorPrint = new Color(255, 149, 237);

    public TechnologyFrontEnd() {
        super("Distributed Systems Frontend");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        setJMenuBar(createMenuBar());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createControlPanel());
        splitPane.setRightComponent(createTablePanel());
        splitPane.setDividerLocation(450);
        getContentPane().add(splitPane);

        loadAllProducts();
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem info = new JMenuItem("Info");
        JMenuItem reload = new JMenuItem("Display Data");

        info.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Frontend client to test REST backend\nUsing HttpURLConnection + XMLPullParser", "Info", JOptionPane.INFORMATION_MESSAGE));
        reload.addActionListener(e -> loadAllProducts());

        menu.add(info);
        menu.add(reload);
        bar.add(menu);
        return bar;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

     // Row 1: Get by Name
        JLabel lblGetName = new JLabel("Get Product by Name:");
        lblGetName.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = row; panel.add(lblGetName, gbc);
        gbc.gridx = 1; txtGetName = new JTextField(12); panel.add(txtGetName, gbc);
        gbc.gridx = 2; JButton btnGet = new JButton("Get"); btnGet.setBackground(Green); panel.add(btnGet, gbc);
        btnGet.addActionListener(this::getByName);


        // Row 2: Delete by ID
        row++;
        JLabel lblDeleteByID = new JLabel("Delete Product by ID:");
        lblDeleteByID.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = row; panel.add(lblDeleteByID, gbc);
        gbc.gridx = 1; txtDeleteId = new JTextField(12); panel.add(txtDeleteId, gbc);
        gbc.gridx = 2; JButton btnDelete = new JButton("Delete"); btnDelete.setBackground(Red); panel.add(btnDelete, gbc);
        btnDelete.addActionListener(this::handleDeleteById);

        // Add product fields row by row
        String[] labels = {"Name", "Type", "Year", "Cost", "Category", "Company ID"};
        JTextField[] fields = new JTextField[] {
                txtName = new JTextField(15),
                txtType = new JTextField(15),
                txtYear = new JTextField(15),
                txtCost = new JTextField(15),
                txtCategory = new JTextField(15),
                txtCompanyId = new JTextField(15)
        };

        for (int i = 0; i < labels.length; i++) {
            row++;
            JLabel lbl = new JLabel(labels[i] + ":");
            lbl.setForeground(Color.WHITE); // ✅ Set label text color to white
            gbc.gridx = 0; gbc.gridy = row;
            panel.add(lbl, gbc);

            gbc.gridx = 1; gbc.gridwidth = 2;
            panel.add(fields[i], gbc);
            gbc.gridwidth = 1;
        }


        // Add Product Button
        row++;
        gbc.gridx = 2; gbc.gridy = row;
        JButton btnAdd = new JButton("Add Product");
        btnAdd.setBackground(Blue);
        panel.add(btnAdd, gbc);
        btnAdd.addActionListener(this::handlePost);

        // Row: Change Price by ID
        row++;
        JLabel lblUpdate = new JLabel("Change Price by ID:");
        lblUpdate.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(lblUpdate, gbc);

        gbc.gridx = 1;
        txtUpdateId = new JTextField(6);
        panel.add(txtUpdateId, gbc);

        gbc.gridx = 2;
        JTextField txtNewPrice = new JTextField(6);
        panel.add(txtNewPrice, gbc);

        row++;
        gbc.gridx = 2; gbc.gridy = row;
        JButton btnEdit = new JButton("Change Price");
        btnEdit.setBackground(Orange);
        panel.add(btnEdit, gbc);
        btnEdit.addActionListener(e -> {
            String idText = txtUpdateId.getText().trim();
            String priceText = txtNewPrice.getText().trim();
            if (idText.isEmpty() || priceText.isEmpty()) {
                showError("Enter both ID and new price.");
                return;
            }
            try {
                int id = Integer.parseInt(idText);
                double newPrice = Double.parseDouble(priceText);

                // Fetch existing product XML by ID
                URL getUrl = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products/" + id);
                HttpURLConnection getCon = (HttpURLConnection) getUrl.openConnection();
                getCon.setRequestMethod("GET");
                getCon.setRequestProperty("Accept", "application/xml");

                Product existingProduct = null;
                if (getCon.getResponseCode() == 200) {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new InputStreamReader(getCon.getInputStream()));

                    existingProduct = new Product();
                    Company company = new Company();
                    String tag = "";
                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                tag = parser.getName();
                                if (tag.equals("company")) company = new Company();
                                break;
                            case XmlPullParser.TEXT:
                                String text = parser.getText();
                                switch (tag) {
                                    case "productid": existingProduct.setProductid(Integer.parseInt(text)); break;
                                    case "name": existingProduct.setName(text); break;
                                    case "type": existingProduct.setType(text); break;
                                    case "year": existingProduct.setYear(Integer.parseInt(text)); break;
                                    case "cost": existingProduct.setCost(Double.parseDouble(text)); break;
                                    case "categoryName": existingProduct.setCategoryName(text); break;
                                    case "companyID": company.setCompanyID(Integer.parseInt(text)); break;
                                    case "companyName": company.setCompanyName(text); break;
                                    case "years": company.setYears(Integer.parseInt(text)); break;
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                if (parser.getName().equals("company")) {
                                    existingProduct.setCompany(company);
                                }
                                break;
                        }
                        eventType = parser.next();
                    }
                } else {
                    showError("Failed to fetch existing product. Code: " + getCon.getResponseCode());
                    return;
                }
                getCon.disconnect();

                existingProduct.setCost(newPrice);

                // Build full XML
                StringBuilder xml = new StringBuilder();
                xml.append("<product>");
                xml.append("<productid>").append(existingProduct.getProductid()).append("</productid>");
                xml.append("<name>").append(existingProduct.getName()).append("</name>");
                xml.append("<type>").append(existingProduct.getType()).append("</type>");
                xml.append("<year>").append(existingProduct.getYear()).append("</year>");
                xml.append("<cost>").append(existingProduct.getCost()).append("</cost>");
                xml.append("<categoryName>").append(existingProduct.getCategoryName()).append("</categoryName>");
                xml.append("<company>");
                xml.append("<companyID>").append(existingProduct.getCompany().getCompanyID()).append("</companyID>");
                xml.append("<companyName>").append(existingProduct.getCompany().getCompanyName()).append("</companyName>");
                xml.append("<years>").append(existingProduct.getCompany().getYears()).append("</years>");
                xml.append("</company>");
                xml.append("</product>");

                URL putUrl = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products/" + id);
                HttpURLConnection putCon = (HttpURLConnection) putUrl.openConnection();
                putCon.setRequestMethod("PUT");
                putCon.setRequestProperty("Content-Type", "application/xml");
                putCon.setDoOutput(true);

                try (OutputStream os = putCon.getOutputStream()) {
                    os.write(xml.toString().getBytes());
                    os.flush();
                }

                if (putCon.getResponseCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Price updated.");
                    loadAllProducts();
                } else {
                    showError("Update failed. Code: " + putCon.getResponseCode());
                }
                putCon.disconnect();
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        // Row: Delete All & Export
        row += 2;
        gbc.gridy = row;
        gbc.gridx = 1;
        JButton btnDeleteAll = new JButton("DELETE ALL");
        btnDeleteAll.setBackground(Red);
        panel.add(btnDeleteAll, gbc);
        btnDeleteAll.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products/all");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("DELETE");
                if (con.getResponseCode() == 204) {
                    JOptionPane.showMessageDialog(this, "All products deleted.");
                    loadAllProducts();
                }
                con.disconnect();
            } catch (Exception ex) {
                showError("Delete all failed: " + ex.getMessage());
            }
        });

        gbc.gridx = 2;
        JButton btnPrint = new JButton("Print to CSV");
        btnPrint.setBackground(colorPrint);
        panel.add(btnPrint, gbc);
        btnPrint.addActionListener(this::handleExport);
        btnPrint.addActionListener(e -> {
            try (PrintWriter pw = new PrintWriter(new File("products_export.csv"))) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    pw.print(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
                for (int row1 = 0; row1 < tableModel.getRowCount(); row1++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        pw.print(tableModel.getValueAt(row1, col));
                        if (col < tableModel.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "Exported to products_export.csv");
            } catch (Exception ex) {
                showError("Export failed: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        String[] columns = { "ID", "Name", "Type", "Year", "Cost", "Category", "Company", "Company Years" };
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setForeground(Color.WHITE);
        productTable.setBackground(Color.BLACK);
        productTable.setGridColor(Color.GRAY);

        JTableHeader header = productTable.getTableHeader();
        header.setBackground(Color.DARK_GRAY);
        header.setForeground(Color.WHITE);

        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        return panel;
    }


    private void loadAllProducts() {
        try {
            URL url = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/xml");

            if (con.getResponseCode() == 200) {
                parseAndDisplayProducts(con);
            } else {
                showError("Failed to fetch products. Code: " + con.getResponseCode());
            }
            con.disconnect();
        } catch (Exception ex) {
            showError("Exception: " + ex.getMessage());
        }
    }

    private void getByName(ActionEvent e) {
        String name = txtGetName.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter product name.");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products/name/" + URLEncoder.encode(name, "UTF-8"));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/xml");

            if (con.getResponseCode() == 200) {
                parseAndDisplayProducts(con);
            } else {
                showError("No product found. Code: " + con.getResponseCode());
            }

            con.disconnect();
        } catch (Exception ex) {
            showError("Error parsing XML: " + ex.getMessage());
        }
    }
    private void handleExport(ActionEvent e) {
        try (PrintWriter pw = new PrintWriter(new File("products_export.csv"))) {
            // Write header
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                pw.print(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) pw.print(",");
            }
            pw.println();

            // Write rows
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    pw.print(tableModel.getValueAt(row, col));
                    if (col < tableModel.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
            }

            pw.flush();
            JOptionPane.showMessageDialog(this, "Exported to products_export.csv");

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("CSV export failed: " + ex.getMessage());
        }
    }

    private void parseAndDisplayProducts(HttpURLConnection con) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new InputStreamReader(con.getInputStream()));

        tableModel.setRowCount(0);
        Product product = null;
        Company company = null;
        String tagName = "";

        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    tagName = parser.getName();
                    if (tagName.equals("product")) product = new Product();
                    else if (tagName.equals("company")) company = new Company();
                    break;

                case XmlPullParser.TEXT:
                    String text = parser.getText();
                    if (product != null) {
                        switch (tagName) {
                            case "productid": product.setProductid(Integer.parseInt(text)); break;
                            case "name": product.setName(text); break;
                            case "type": product.setType(text); break;
                            case "year": product.setYear(Integer.parseInt(text)); break;
                            case "cost": product.setCost(Double.parseDouble(text)); break;
                            case "categoryName": product.setCategoryName(text); break;
                            case "companyID": if (company != null) company.setCompanyID(Integer.parseInt(text)); break;
                            case "companyName": if (company != null) company.setCompanyName(text); break;
                            case "years": if (company != null) company.setYears(Integer.parseInt(text)); break;
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("company") && product != null) product.setCompany(company);
                    else if (parser.getName().equals("product")) {
                        Company c = product.getCompany();
                        tableModel.addRow(new Object[]{
                                product.getProductid(), product.getName(), product.getType(),
                                product.getYear(), product.getCost(), product.getCategoryName(),
                                c != null ? c.getCompanyName() : "", c != null ? c.getYears() : ""
                        });
                        product = null;
                        company = null;
                    }
                    break;
            }
            event = parser.next();
        }
    }

    private void handleDeleteById(ActionEvent e) {
        String idText = txtDeleteId.getText().trim();
        if (idText.isEmpty()) {
            showError("Enter a product ID to delete.");
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            URL url = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");

            if (con.getResponseCode() == 204) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully.");
                loadAllProducts();
            } else {
                showError("Delete failed. Code: " + con.getResponseCode());
            }
            con.disconnect();
        } catch (Exception ex) {
            showError("Exception: " + ex.getMessage());
        }
    }
    private int generateRandomId() {
        return (int)(1000 + Math.random() * 9000); // random 4-digit ID
    }

    private void handlePost(ActionEvent e) {
        try {
            // Gather input from text fields
            String name = txtName.getText().trim();
            String type = txtType.getText().trim();
            String yearStr = txtYear.getText().trim();
            String costStr = txtCost.getText().trim();
            String category = txtCategory.getText().trim();
            String companyIdStr = txtCompanyId.getText().trim();

            // Validate inputs
            if (name.isEmpty() || type.isEmpty() || yearStr.isEmpty() || costStr.isEmpty() ||
                category.isEmpty() || companyIdStr.isEmpty()) {
                showError("Please fill in all product fields.");
                return;
            }

            int year = Integer.parseInt(yearStr);
            double cost = Double.parseDouble(costStr);
            int companyId = Integer.parseInt(companyIdStr);
            int productId = generateRandomId(); // or request from server if needed

            // XML body to send in POST
            String xml = "" +
                    "<product>" +
                    "<productid>" + productId + "</productid>" +
                    "<name>" + name + "</name>" +
                    "<type>" + type + "</type>" +
                    "<year>" + year + "</year>" +
                    "<cost>" + cost + "</cost>" +
                    "<categoryName>" + category + "</categoryName>" +
                    "<company>" +
                    "<companyID>" + companyId + "</companyID>" +
                    "</company>" +
                    "</product>";

            // Send HTTP POST request
            URL url = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/xml");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(xml.getBytes());
                os.flush();
            }

            if (con.getResponseCode() == 201) {
                JOptionPane.showMessageDialog(this, "Product added successfully.");
                loadAllProducts();
            } else {
                showError("POST failed. Code: " + con.getResponseCode());
            }

            con.disconnect();
        } catch (Exception ex) {
            showError("Exception: " + ex.getMessage());
        }
    }


    private void handlePut(ActionEvent e) {
        try {
            int id = 999; // Update the one we added
            URL url = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/xml");
            con.setDoOutput(true);

            String xml = "" +
                    "<product>" +
                    "<productid>999</productid>" +
                    "<name>UpdatedSmartLight</name>" +
                    "<type>Gadget</type>" +
                    "<year>2025</year>" +
                    "<cost>300</cost>" +
                    "<categoryName>Smart Home</categoryName>" +
                    "<company>" +
                    "<companyID>1</companyID>" +
                    "<companyName>BrightTech</companyName>" +
                    "<years>11</years>" +
                    "</company>" +
                    "</product>";

            try (OutputStream os = con.getOutputStream()) {
                os.write(xml.getBytes());
                os.flush();
            }

            if (con.getResponseCode() == 200) {
                JOptionPane.showMessageDialog(this, "Product updated.");
                loadAllProducts();
            } else {
                showError("PUT failed. Code: " + con.getResponseCode());
            }
            con.disconnect();
        } catch (Exception ex) {
            showError("Exception: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TechnologyFrontEnd().setVisible(true));
    }
}
