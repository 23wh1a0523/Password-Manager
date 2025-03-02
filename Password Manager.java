package projectjava;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.util.Optional;

public class PasswordManagerAppGUI extends JFrame {
    private static final String USERS_FILE = "users.txt"; // File to store usernames and passwords
    private static PasswordManager passwordManager;
    private static String currentUser = null;

    public PasswordManagerAppGUI() {
        showLoginRegisterScreen();
    }

    private void showLoginRegisterScreen() {
        JFrame frame = new JFrame("Password Manager - Login or Register");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeLoginRegisterComponents(panel);

        frame.setVisible(true);
    }

    private void placeLoginRegisterComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 80, 80, 25);
        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());
            if (login(username, password)) {
                panel.getTopLevelAncestor().setVisible(false);
                showMainMenu();
            } else {
                JOptionPane.showMessageDialog(panel, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(180, 80, 100, 25);
        registerButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());
            register(username, password);
        });
        panel.add(registerButton);
    }

    private void showMainMenu() {
        JFrame frame = new JFrame("Password Manager - Main Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1));

        JButton addButton = new JButton("Add Password");
        addButton.addActionListener(e -> addPasswordDialog());
        panel.add(addButton);

        JButton searchButton = new JButton("Search Password");
        searchButton.addActionListener(e -> searchPasswordDialog());
        panel.add(searchButton);

        JButton listButton = new JButton("List All Passwords");
        listButton.addActionListener(e -> listPasswordsDialog());
        panel.add(listButton);

        JButton deleteButton = new JButton("Delete Password");
        deleteButton.addActionListener(e -> deletePasswordDialog());
        panel.add(deleteButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            currentUser = null;
            frame.setVisible(false);
            showLoginRegisterScreen();
        });
        panel.add(logoutButton);

        frame.add(panel);
        frame.setVisible(true);

        passwordManager = new PasswordManager(currentUser);
    }

    private void addPasswordDialog() {
        String website = JOptionPane.showInputDialog("Enter Website:");
        String username = JOptionPane.showInputDialog("Enter Username:");
        String password = JOptionPane.showInputDialog("Enter Password:");

        PasswordEntry entry = new PasswordEntry(website, username, password);
        passwordManager.addPassword(entry);
        JOptionPane.showMessageDialog(null, "Password added successfully.");
    }

    private void searchPasswordDialog() {
        String website = JOptionPane.showInputDialog("Enter Website to search:");
        Optional<PasswordEntry> result = passwordManager.getPasswordByWebsite(website);

        if (result.isPresent()) {
            JOptionPane.showMessageDialog(null, "Found: " + result.get());
        } else {
            JOptionPane.showMessageDialog(null, "No entry found for the website.");
        }
    }

    private void listPasswordsDialog() {
        java.util.List<PasswordEntry> passwords = passwordManager.getAllPasswords();
        if (passwords.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No passwords stored.");
        } else {
            StringBuilder list = new StringBuilder("Stored Passwords:\n");
            for (PasswordEntry entry : passwords) {
                list.append(entry).append("\n");
            }
            JOptionPane.showMessageDialog(null, list.toString());
        }
    }

    private void deletePasswordDialog() {
        String website = JOptionPane.showInputDialog("Enter Website to delete:");
        if (passwordManager.deletePassword(website)) {
            JOptionPane.showMessageDialog(null, "Password deleted successfully.");
        } else {
            JOptionPane.showMessageDialog(null, "No entry found for the website.");
        }
    }

    private void register(String username, String password) {
        try {
            if (userExists(username)) {
                JOptionPane.showMessageDialog(null, "Username already exists. Try a different username.");
            } else {
                try (FileWriter writer = new FileWriter(USERS_FILE, true)) {
                    writer.write(username + ":" + password + "\n"); // Save username and password
                    JOptionPane.showMessageDialog(null, "Registration successful! You can now log in.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error during registration: " + e.getMessage());
        }
    }

    private boolean login(String username, String password) {
        try {
            if (authenticateUser(username, password)) {
                currentUser = username;
                return true;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error during login: " + e.getMessage());
        }
        return false;
    }

    private boolean userExists(String username) throws IOException {
        if (!Files.exists(Paths.get(USERS_FILE))) return false;

        return Files.lines(Paths.get(USERS_FILE))
                .anyMatch(line -> line.split(":")[0].equals(username));
    }

    private boolean authenticateUser(String username, String password) throws IOException {
        if (!Files.exists(Paths.get(USERS_FILE))) return false;

        return Files.lines(Paths.get(USERS_FILE))
                .anyMatch(line -> {
                    String[] parts = line.split(":");
                    return parts[0].equals(username) && parts[1].equals(password);
                });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PasswordManagerAppGUI::new);
    }
}
