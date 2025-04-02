package frontPack;

import javax.swing.*;
import javax.swing.table.*;
import javax.xml.bind.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.List;

public class TechnologyFrontEnd extends JFrame {

    private JTextField txtGetName, txtDeleteId;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public TechnologyFrontEnd() {
        super("Distributed Systems Frontend");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 600);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);
        setLocationRelativeTo(null);

        setJMenuBar(createMenuBar());
        add(createControlPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        loadAllProducts();
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem info = new JMenuItem("Info");
        JMenuItem reload = new JMenuItem("Display Data");

        info.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Frontend client to test REST backend\nUsing HttpURLConnection + JAXB", "Info", JOptionPane.INFORMATION_MESSAGE));
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
        panel.setPreferredSize(new Dimension(250, 600));

        txtGetName = createInputField(panel, "Get Product by Name:");
        JButton btnGet = createButton("Get", this::handleGetByName);
        panel.add(btnGet);

        txtDeleteId = createInputField(panel, "Delete Product by ID:");
        JButton btnDelete = createButton("Delete", this::handleDeleteById);
        panel.add(btnDelete);

        JButton btnPost = createButton("Add New Product (TODO)", e -> JOptionPane.showMessageDialog(this, "POST not implemented."));
        JButton btnPut = createButton("Update Product (TODO)", e -> JOptionPane.showMessageDialog(this, "PUT not implemented."));

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
                JAXBContext jaxb = JAXBContext.newInstance(ProductsWrapper.class);
                ProductsWrapper wrapper = (ProductsWrapper) jaxb.createUnmarshaller().unmarshal(con.getInputStream());

                tableModel.setRowCount(0);
                for (Product p : wrapper.getProducts()) {
                    Company c = p.getCompany();
                    tableModel.addRow(new Object[]{
                            p.getProductid(), p.getName(), p.getType(), p.getYear(), p.getCost(),
                            p.getCategoryName(), c != null ? c.getCompanyName() : "",
                            c != null ? c.getYears() : ""
                    });
                }
            } else {
                showError("Failed to fetch products. Code: " + con.getResponseCode());
            }

            con.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Exception: " + ex.getMessage());
        }
    }

    private void handleGetByName(ActionEvent e) {
        String name = txtGetName.getText().trim();
        if (name.isEmpty()) {
            showError("Enter a product name.");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/ProjectDistributedBackend/rest/products/name/" + name);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/xml");

            if (con.getResponseCode() == 200) {
                JAXBContext jaxb = JAXBContext.newInstance(Product.class, ProductsWrapper.class);
                Unmarshaller um = jaxb.createUnmarshaller();

                Object result = um.unmarshal(con.getInputStream());

                tableModel.setRowCount(0);
                if (result instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Product> list = (List<Product>) result;
                    for (Product p : list) {
                        Company c = p.getCompany();
                        tableModel.addRow(new Object[]{
                                p.getProductid(), p.getName(), p.getType(), p.getYear(), p.getCost(),
                                p.getCategoryName(), c != null ? c.getCompanyName() : "",
                                c != null ? c.getYears() : ""
                        });
                    }
                } else if (result instanceof Product) {
                    Product p = (Product) result;
                    Company c = p.getCompany();
                    tableModel.addRow(new Object[]{
                            p.getProductid(), p.getName(), p.getType(), p.getYear(), p.getCost(),
                            p.getCategoryName(), c != null ? c.getCompanyName() : "",
                            c != null ? c.getYears() : ""
                    });
                }

            } else {
                showError("No product found. Code: " + con.getResponseCode());
            }

            con.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Exception: " + ex.getMessage());
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
        } catch (NumberFormatException ex) {
            showError("ID must be a number.");
        } catch (Exception ex) {
            ex.printStackTrace();
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
