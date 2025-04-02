package frontPack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TechnologyFrontEnd extends JFrame {

    private JTextField txtGetName, txtDeleteId;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public TechnologyFrontEnd() {
        super("Distributed Systems Frontend");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);

        setJMenuBar(createMenuBar());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createControlPanel());
        splitPane.setRightComponent(createTablePanel());
        splitPane.setDividerLocation(280);
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
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        txtGetName = createInputField(panel, "Get Product by Name:");
        JButton btnGet = createButton("Get", this::getByName);
        panel.add(btnGet);

        txtDeleteId = createInputField(panel, "Delete Product by ID:");
        JButton btnDelete = createButton("Delete", this::handleDeleteById);
        panel.add(btnDelete);

        JButton btnPost = createButton("Add Product", this::handlePost);
        JButton btnPut = createButton("Update Product", this::handlePut);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(btnPost);
        panel.add(btnPut);

        return panel;
    }

    private JTextField createInputField(JPanel panel, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.WHITE);
        JTextField txt = new JTextField();
        txt.setMaximumSize(new Dimension(220, 25));
        panel.add(lbl);
        panel.add(txt);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        return txt;
    }

    private JButton createButton(String text, java.awt.event.ActionListener handler) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 30));
        btn.addActionListener(handler);
        return btn;
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

    private void handlePost(ActionEvent e) {
        try {
            URL url = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/xml");
            con.setDoOutput(true);

            String xml = "" +
                    "<product>" +
                    "<productid>999</productid>" +
                    "<name>SmartLight</name>" +
                    "<type>Gadget</type>" +
                    "<year>2024</year>" +
                    "<cost>250</cost>" +
                    "<categoryName>Home Automation</categoryName>" +
                    "<company>" +
                    "<companyID>1</companyID>" +
                    "<companyName>BrightTech</companyName>" +
                    "<years>10</years>" +
                    "</company>" +
                    "</product>";

            try (OutputStream os = con.getOutputStream()) {
                os.write(xml.getBytes());
                os.flush();
            }

            if (con.getResponseCode() == 201) {
                JOptionPane.showMessageDialog(this, "Product added.");
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
