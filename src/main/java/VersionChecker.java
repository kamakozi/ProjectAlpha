import com.google.firebase.database.*;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;

public class VersionChecker {
    private static final String CURRENT_VERSION = "0.02";  // Change this when updating
    private static final DatabaseReference database = FirebaseDatabase.getInstance().getReference("versionControll");

    public static void checkForUpdates() {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double latestVersion = snapshot.child("latest_version").getValue(Double.class);
                    Boolean requireUpdate = snapshot.child("require_update").getValue(Boolean.class);
                    String downloadUrl = "https://github.com/YOUR_GITHUB_USERNAME/ProjectAlpha/releases/latest/download/ProjectAlphaV1.jar"; // Change this link when updating

                    if (latestVersion != null && requireUpdate != null && requireUpdate) {
                        if (!CURRENT_VERSION.equals(latestVersion.toString())) {
                            System.out.println("🚀 A new update is available! Version: " + latestVersion);
                            try {
                                downloadFile(downloadUrl, "ProjectAlphaV1-" + latestVersion + ".jar");
                            } catch (IOException e) {
                                System.err.println("❌ Failed to download update: " + e.getMessage());
                            }
                        } else {
                            System.out.println("✅ You are using the latest version.");
                        }
                    } else {
                        System.out.println("✅ No update required.");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("❌ Error checking version: " + error.getMessage());
            }
        });
    }

    // 📌 Method to Download the JAR File Automatically
    private static void downloadFile(String fileURL, String saveFileName) throws IOException {
        URL url = new URL(fileURL);
        Path targetPath = Paths.get(System.getProperty("user.home"), "Downloads", saveFileName);
        Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("✅ Update downloaded: " + targetPath.toString());
    }
}