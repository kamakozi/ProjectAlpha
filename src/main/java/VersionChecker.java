import com.google.firebase.database.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class VersionChecker {
    private static final String CURRENT_VERSION = "0.05";  // ✅ Update this when releasing new versions
    private static final DatabaseReference database = FirebaseDatabase.getInstance().getReference("versionControll");

    public static boolean checkForUpdates() {
        final boolean[] updateRequired = {false};

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double latestVersion = snapshot.child("latest_version").getValue(Double.class);
                    Boolean requireUpdate = snapshot.child("require_update").getValue(Boolean.class);
                    String downloadUrl = snapshot.child("download_url").getValue(String.class);

                    if (latestVersion != null && requireUpdate != null && requireUpdate) {
                        if (!CURRENT_VERSION.equals(latestVersion.toString())) {
                            System.out.println("🚀 A new update is available! Version: " + latestVersion);
                            try {
                                String newJarPath = downloadFile(downloadUrl, "ProjectAlphaV1-" + latestVersion + ".jar");

                                // ✅ Restart with the new JAR
                                restartWithNewJar(newJarPath);

                            } catch (IOException e) {
                                System.err.println("❌ Failed to download update: " + e.getMessage());
                            }
                            updateRequired[0] = true;
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

        return updateRequired[0];
    }

    // 📌 Fixed Download Function
    private static String downloadFile(String fileURL, String saveFileName) throws IOException {
        if (fileURL == null || fileURL.isEmpty()) {
            throw new IOException("❌ No valid download URL provided.");
        }

        URL url = new URL(fileURL);
        Path targetPath = Paths.get(System.getProperty("user.home"), "Downloads", saveFileName);

        try (InputStream in = url.openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Update downloaded: " + targetPath.toString());
        }

        return targetPath.toString();  // ✅ Return the downloaded JAR path
    }

    // 📌 Restart with the new JAR
    private static void restartWithNewJar(String jarPath) {
        System.out.println("🔄 Restarting with new version...");

        try {
            // ✅ Launch the new JAR file
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", jarPath);
            builder.start();

            // ✅ Exit current application
            System.exit(0);
        } catch (IOException e) {
            System.err.println("❌ Failed to restart with new JAR: " + e.getMessage());
        }
    }
}