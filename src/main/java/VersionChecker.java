import com.google.firebase.database.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VersionChecker {
    private static final String CURRENT_VERSION = "0.15";
    private static final DatabaseReference database = FirebaseDatabase.getInstance().getReference("versionControll");

    public static void checkForUpdates(ProjectAlpha appInstance, Runnable callback) {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double latestVersion = snapshot.child("latest_version").getValue(Double.class);
                    Boolean requireUpdate = snapshot.child("require_update").getValue(Boolean.class);
                    String downloadUrl = snapshot.child("download_url").getValue(String.class);

                    if (latestVersion != null && requireUpdate != null && requireUpdate && !CURRENT_VERSION.equals(latestVersion.toString())) {
                        System.out.println("🚀 A new update is available! Version: " + latestVersion);
                        appInstance.closeLoginDialog();

                        showFakeDownloadAnimation(() -> {
                            try {
                                String newJarPath = downloadFile(downloadUrl, "ProjectAlphaV1-" + latestVersion + ".jar");

                                showFakePatchingAnimation(() -> {
                                    restartWithNewJar(newJarPath);
                                });

                            } catch (IOException e) {
                                System.err.println("❌ Failed to download update: " + e.getMessage());
                                callback.run();
                            }
                        });

                    } else {
                        System.out.println("✅ No update required. Proceeding to login.");
                        SwingUtilities.invokeLater(callback);
                    }
                } else {
                    System.out.println("✅ No version info found, skipping update check.");
                    SwingUtilities.invokeLater(callback);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("❌ Error checking version: " + error.getMessage());
                SwingUtilities.invokeLater(callback);
            }
        });
    }

    private static void showFakeDownloadAnimation(Runnable onComplete) {
        JDialog updateDialog = new JDialog();
        updateDialog.setTitle("Downloading Update...");
        updateDialog.setSize(400, 150);
        updateDialog.setUndecorated(true);
        updateDialog.setLocationRelativeTo(null);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Downloading new version..."), BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        updateDialog.add(panel);
        updateDialog.setVisible(true);

        Timer timer = new Timer(400, new ActionListener() {
            int progress = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 10;
                progressBar.setValue(progress);
                if (progress > 100) {
                    updateDialog.dispose();
                    ((Timer) e.getSource()).stop();
                    onComplete.run();
                }
            }
        });
        timer.start();
    }

    private static void showFakePatchingAnimation(Runnable onComplete) {
        JDialog patchDialog = new JDialog();
        patchDialog.setTitle("Patching Update...");
        patchDialog.setSize(400, 150);
        patchDialog.setUndecorated(true);
        patchDialog.setLocationRelativeTo(null);
        patchDialog.add(new JLabel("Applying update, please wait...", SwingConstants.CENTER));
        patchDialog.setVisible(true);

        Timer patchTimer = new Timer(5000, e -> {
            patchDialog.dispose();
            ((Timer) e.getSource()).stop();
            onComplete.run();
        });
        patchTimer.setRepeats(false);
        patchTimer.start();
    }

    private static String downloadFile(String fileURL, String saveFileName) throws IOException {
        if (fileURL == null || fileURL.isEmpty()) {
            throw new IOException("❌ No valid download URL provided.");
        }

        System.out.println("🔗 Downloading update from: " + fileURL);
        URL url = new URL(fileURL);
        Path targetPath = Paths.get(System.getProperty("user.home"), "Downloads", saveFileName);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/octet-stream");

        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Update downloaded successfully: " + targetPath.toString());
        } catch (IOException e) {
            System.err.println("❌ ERROR: Failed to download file. Check your URL and network connection.");
            throw e;
        }

        return targetPath.toString();
    }

    private static void restartWithNewJar(String jarPath) {
        System.out.println("🔄 Restarting with new version...");

        try {
            new ProcessBuilder("java", "-jar", jarPath).start();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("❌ Failed to restart with new JAR: " + e.getMessage());
        }
    }
}