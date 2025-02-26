import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ProjectAlpha {
    private JFrame frame;
    private JDialog loginDialog;  // Store login window instance

    public ProjectAlpha() {
        FirebaseAuth.initializeFirebase();
        checkForUpdates();
    }

    private void checkForUpdates() {
        VersionChecker.checkForUpdates(this, this::showLoginPopup);
    }

    public void closeLoginDialog() {
        if (loginDialog != null) {
            loginDialog.dispose();  // ✅ Closes the login popup when updating
        }
    }

    private void showLoginPopup() {
        loginDialog = new JDialog();
        loginDialog.setTitle("Login Required");
        loginDialog.setSize(300, 150);
        loginDialog.setLayout(new BorderLayout());
        loginDialog.setModal(true);
        loginDialog.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Enter Access Key:", SwingConstants.CENTER);
        JTextField inputField = new JTextField();
        JButton loginButton = new JButton("Login");

        panel.add(label);
        panel.add(inputField);
        panel.add(loginButton);
        loginDialog.add(panel, BorderLayout.CENTER);

        FirebaseAuth firebaseAuth = new FirebaseAuth();

        loginButton.addActionListener(e -> {
            if (firebaseAuth.verifyUserKey(inputField.getText())) {
                loginDialog.dispose();
                showLoadingScreen();
            } else {
                JOptionPane.showMessageDialog(loginDialog, "❌ Invalid Key! Try Again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginDialog.setVisible(true);
    }

    private void showLoadingScreen() {
        JDialog loadingDialog = new JDialog();
        loadingDialog.setSize(500, 350);
        loadingDialog.setUndecorated(true);
        loadingDialog.setResizable(false);
        loadingDialog.setLocationRelativeTo(null);

        JLabel gifLabel = new JLabel(new ImageIcon(getClass().getResource("/giphy.gif")), SwingConstants.CENTER);
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.BLACK);
        loadingPanel.add(gifLabel, BorderLayout.CENTER);

        loadingDialog.add(loadingPanel);
        loadingDialog.setVisible(true);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();
                    loadMainInterface();
                });
            }
        }, 4000 + (int) (Math.random() * 3000)); // Random delay between 4-7 sec
    }

    private void loadMainInterface() {
        frame = new JFrame("MemeCoin Creator");
        frame.setSize(1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setMinimumSize(new Dimension(1000, 600));

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(250, frame.getHeight()));
        sidePanel.setBackground(new Color(20, 20, 20));

        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(createStyledButton("Deposit"));
        sidePanel.add(Box.createVerticalStrut(15));
        sidePanel.add(createStyledButton("Create Meme Coin"));
        sidePanel.add(Box.createVerticalStrut(15));
        sidePanel.add(createStyledButton("Auto Buy"));
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(createStyledButton("User Info"));
        sidePanel.add(Box.createVerticalStrut(15));
        sidePanel.add(createStyledButton("Settings"));

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(20, 20, 20));

        frame.add(sidePanel, BorderLayout.WEST);
        frame.add(mainContent, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(40, 40, 40));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    public static void main(String[] args) {
        FirebaseAuth.initializeFirebase();
        SwingUtilities.invokeLater(ProjectAlpha::new);
    }
}