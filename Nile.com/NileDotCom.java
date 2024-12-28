
// All useful imports
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

public class NileDotCom extends JFrame implements ActionListener {
    // GUI components
    private JTextField itemIdField, quantityField, detailsField, subtotalField;
    private JTextArea cartArea, invoiceArea;
    private JButton addButton, removeButton, displayCartButton, checkoutButton, clearCartButton, exitButton;
    private int count = 1;
    private JLabel cartItemCountLabel;
    
    // Data structures
    private Map<String, Item> inventory;
    private java.util.List<CartItem> cart;

    // Constructor
    public NileDotCom() {
        super("Nile Dot Com");
        setLayout(new BorderLayout());

        // Initializing data structures
        inventory = new HashMap<>();
        cart = new ArrayList<>();

        setupGui();

        loadInventory();

        // Set window properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
    }

    private void setupGui() {
    // Panel for item input fields
    JPanel inputPanel = new JPanel(new GridLayout(5, 2));
    itemIdField = new JTextField();
    quantityField = new JTextField();
    detailsField = new JTextField();
    subtotalField = new JTextField();
    
    inputPanel.add(new JLabel("Item ID #" + count + ":"));
    inputPanel.add(itemIdField);
    inputPanel.add(new JLabel("Quantity:"));
    inputPanel.add(quantityField);
    inputPanel.add(new JLabel("Details for Item #" + count + ": "));
    inputPanel.add(detailsField);
    inputPanel.add(new JLabel("Current Subtotal for " + count + " Items: "));
    inputPanel.add(subtotalField);

    //Adding the input panel to the top 
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(inputPanel, BorderLayout.CENTER);

    //Item count
    cartItemCountLabel = new JLabel("Items in Cart: 0");
    topPanel.add(cartItemCountLabel, BorderLayout.SOUTH);

    add(topPanel, BorderLayout.NORTH);

    // Buttons panel
    JPanel buttonPanel = new JPanel(new GridLayout(2, 3));
    addButton = new JButton("Add to Cart");
    removeButton = new JButton("Remove from Cart");
    displayCartButton = new JButton("Display Cart");
    checkoutButton = new JButton("Checkout");
    clearCartButton = new JButton("Clear Cart");
    exitButton = new JButton("Exit");

    //Adding to the panel
    buttonPanel.add(addButton);
    buttonPanel.add(removeButton);
    buttonPanel.add(displayCartButton);
    buttonPanel.add(checkoutButton);
    buttonPanel.add(clearCartButton);
    buttonPanel.add(exitButton);
    
    add(buttonPanel, BorderLayout.SOUTH);

    // Cart area
    cartArea = new JTextArea(10, 40);
    cartArea.setEditable(false);
    add(new JScrollPane(cartArea), BorderLayout.CENTER);

    // Action listeners
    addButton.addActionListener(this);
    removeButton.addActionListener(this);
    displayCartButton.addActionListener(this);
    checkoutButton.addActionListener(this);
    clearCartButton.addActionListener(this);
    exitButton.addActionListener(this);
}


    private void loadInventory() {
    try (BufferedReader br = new BufferedReader(new FileReader("Nile.com/inventory.csv"))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 5) {
                String id = parts[0].trim();
                String description = parts[1].trim();
                boolean inStock = Boolean.parseBoolean(parts[2].trim());
                int quantity = Integer.parseInt(parts[3].trim());
                double price = Double.parseDouble(parts[4].trim());
                inventory.put(id, new Item(id, description, inStock, quantity, price));
            } else {
                System.out.println("Skipping invalid line: " + line);
            }
        }

        // Print the inventory to verify it was loaded
        System.out.println("Inventory loaded successfully:");
        for (Map.Entry<String, Item> entry : inventory.entrySet()) {
            Item item = entry.getValue();
            System.out.println("ID: " + item.id + ", Description: " + item.description + ", In Stock: " + item.inStock + ", Quantity: " + item.quantity + ", Price: $" + item.price);
        }

    } catch (FileNotFoundException e) {
        JOptionPane.showMessageDialog(this, "Inventory file not found: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Error reading inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Error in inventory file format: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    // This is here to chedck if any of these buttons were clicked
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addItemToCart();
        } else if (e.getSource() == removeButton) {
            removeItemFromCart();
        } else if (e.getSource() == displayCartButton) {
            displayCart();
        } else if (e.getSource() == checkoutButton) {
            checkout();
        } else if (e.getSource() == clearCartButton) {
            clearCart();
        } else if (e.getSource() == exitButton) {
            System.exit(0);
        }
    }

    //For real time updates with the cart item count based on the cart size
    private void updateCartItemCount() {
        cartItemCountLabel.setText("Items in Cart: " + cart.size());
    }

    private void addItemToCart() {
    String itemId = itemIdField.getText().trim();
    String quantityStr = quantityField.getText().trim();

    if (itemId.isEmpty() || quantityStr.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Item ID and Quantity are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    int quantity;
    try {
        quantity = Integer.parseInt(quantityStr);
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    Item item = inventory.get(itemId);
    if (item == null) {
        JOptionPane.showMessageDialog(this, "Item not found.", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (item.quantity < quantity) {
        JOptionPane.showMessageDialog(this, "Not enough stock available.", "Stock Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Add item to cart
    cart.add(new CartItem(item, quantity));
    item.quantity -= quantity;

    // Update the item details
    String details = String.format("ID: %s | Description: %s | Quantity: %d | Unit Price: $%.2f | Total: $%.2f", 
                                   item.id, item.description, quantity, item.price, item.price * quantity);
    detailsField.setText(details);

    // Calculate the subtotal
    double subtotal = 0.0;
    for (CartItem cartItem : cart) {
        subtotal += cartItem.getTotalPrice();
    }
    subtotalField.setText(String.format("$%.2f", subtotal));

    // Update the label for item ID input and clear the fields
    count++;
    ((JLabel) ((JPanel) itemIdField.getParent()).getComponent(0)).setText("Item ID #" + count + ":");
    itemIdField.setText("");
    quantityField.setText("");

    // Show success message if item is properly added into cart
    JOptionPane.showMessageDialog(this, "Item added to cart.", "Success", JOptionPane.INFORMATION_MESSAGE);

    // Update the GUI for the next item with count
    updateGuiForNextItem();
    updateCartItemCount();

}

private void updateGuiForNextItem() {
    // Update labels and fields to reflect the addition of the new item
    ((JLabel) ((JPanel) itemIdField.getParent()).getComponent(0)).setText("Item ID #" + count + ":");
    ((JLabel) ((JPanel) detailsField.getParent()).getComponent(4)).setText("Details for Item #" + (count-1) + ": ");
    ((JLabel) ((JPanel) subtotalField.getParent()).getComponent(6)).setText("Current Subtotal for " + cart.size() + " Items: ");
}

    private void removeItemFromCart() {
        String itemId = itemIdField.getText().trim();

        //First check to see if Item ID field is empty, if it is an error message pops up
        if (itemId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item ID is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CartItem toRemove = null;
        
        //Loop through the cart to find the item that matches the ID
        for (CartItem cartItem : cart) {
            if (cartItem.item.id.equals(itemId)) {
                toRemove = cartItem;
                break;
            }
        }

        //When we find the item remove it from the cart and update stock quantity
        if (toRemove != null) {
            cart.remove(toRemove);
            toRemove.item.quantity += toRemove.quantity;
            JOptionPane.showMessageDialog(this, "Item removed from cart.", "Success", JOptionPane.INFORMATION_MESSAGE);
            updateCartItemCount();
        } else {
            JOptionPane.showMessageDialog(this, "Item not found in cart.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Function for displaying the cart in the shopping list
    private void displayCart() {
        cartArea.setText("");
        if (cart.isEmpty()) {
            cartArea.append("Your cart is empty.");
        } else {
            for (CartItem cartItem : cart) {
                cartArea.append(cartItem.item.description + " - Quantity: " + cartItem.quantity + "\n");
            }
        }
    }

    private void checkout() {
    // if cart is empty it doesnt checkout
    if (cart.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Your cart is empty.", "Checkout Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    double subtotal = 0.0;
    for (CartItem cartItem : cart) {
        subtotal += cartItem.getTotalPrice();
    }
    double tax = subtotal * 0.06;
    double total = subtotal + tax;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String dateTime = sdf.format(new Date());

    //The ivoice that is produced after checking out
    StringBuilder invoice = new StringBuilder();
    invoice.append("Nile Dot Com\n");
    invoice.append("---------------\n");
    invoice.append("Date: ").append(dateTime).append("\n\n");
    invoice.append("Item#\tDescription\tQuantity\tUnit Price\tTotal\n");

    int itemNumber = 1;
    for (CartItem cartItem : cart) {
        invoice.append(itemNumber++).append("\t")
               .append(cartItem.item.description).append("\t")
               .append(cartItem.quantity).append("\t")
               .append("$").append(String.format("%.2f", cartItem.item.price)).append("\t")
               .append("$").append(String.format("%.2f", cartItem.getTotalPrice())).append("\n");
    }
    // All the number values in the invoice
    invoice.append("\nSubtotal:\t$").append(String.format("%.2f", subtotal)).append("\n");
    invoice.append("Tax (6%):\t$").append(String.format("%.2f", tax)).append("\n");
    invoice.append("Total:\t$").append(String.format("%.2f", total)).append("\n");

    JOptionPane.showMessageDialog(this, invoice.toString(), "Invoice", JOptionPane.INFORMATION_MESSAGE);

    // Define the file path for the transactions log
    String filePath = "transactions.csv";
    System.out.println("Current Directory: " + new File(".").getAbsolutePath());

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
        for (CartItem cartItem : cart) {
            String itemTime = sdf.format(new Date()); // Capture the time for each item
            bw.write(cartItem.item.id + "," + cartItem.item.description + "," + cartItem.quantity + "," 
                     + cartItem.item.price + "," + cartItem.getTotalPrice() + "," + itemTime + "\n");
        }
        bw.write("Transaction Time: " + dateTime + ", Subtotal: " + subtotal + ", Tax: " + tax + ", Total: " + total + "\n");
        bw.write("----\n"); // Separator for readability in the log file
        System.out.println("Transaction logged successfully.");
    } catch (IOException ex) {
        ex.printStackTrace(); // Print stack trace for debugging
        JOptionPane.showMessageDialog(this, "Error logging transaction: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    cart.clear(); // Clear the cart after checkout
    cartArea.setText(""); // Clear the cart display
    cartItemCountLabel.setText("Items in Cart: 0"); // Reset the item count label
    JOptionPane.showMessageDialog(this, "Checkout complete. Invoice generated.", "Success", JOptionPane.INFORMATION_MESSAGE);
}

    // Clearing the cart function that will display a message
    private void clearCart() {
        cart.clear();
        cartArea.setText("");
        updateCartItemCount(); 
        JOptionPane.showMessageDialog(this, "Cart cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new NileDotCom();
    }
}

class Item {
    String id;
    String description;
    boolean inStock;
    int quantity;
    double price;

    public Item(String id, String description, boolean inStock, int quantity, double price) {
        this.id = id;
        this.description = description;
        this.inStock = inStock;
        this.quantity = quantity;
        this.price = price;
    }
}

class CartItem {
    Item item; 
    int quantity;  

    // Constructor
    public CartItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    // Method to calculate the total price for the item based on its quantity
    public double getTotalPrice() {
        return item.price * quantity; 
    }
}