import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;

public class FirebaseAuth {
    private static DatabaseReference database;

    public static void initializeFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream serviceAccount = FirebaseAuth.class.getResourceAsStream("/memecoinserverauth.json");
                if (serviceAccount == null) {
                    throw new RuntimeException("❌ Firebase config file not found!");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://memecoin-auth-default-rtdb.europe-west1.firebasedatabase.app")
                        .build();

                FirebaseApp.initializeApp(options);
                database = FirebaseDatabase.getInstance().getReference("users");
                System.out.println("✅ Firebase initialized successfully!");

                // 🔹 DEBUG: Fetch sample data from Firebase
                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        System.out.println("🔥 Firebase Test: " + snapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.err.println("❌ Firebase Read Error: " + error.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ Error initializing Firebase: " + e.getMessage());
            }
        }
    }

    public boolean verifyUserKey(String enteredKey, Runnable onSuccess, Runnable onFailure) {
        if (database == null) {
            System.err.println("❌ Database is NULL. Cannot verify user key.");
            SwingUtilities.invokeLater(onFailure);
            return false;
        }

        String hwid = getHWID();
        System.out.println("🔍 Checking HWID: " + hwid);

        CompletableFuture.runAsync(() -> {
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean valid = false;

                    System.out.println("🔥 Firebase Users Data: " + snapshot.getValue());  // DEBUG: Print full data

                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String storedKey = String.valueOf(userSnapshot.child("key").getValue());
                            System.out.println("🔹 Checking user key: " + storedKey);

                            if (storedKey.equals(enteredKey)) {
                                String storedHWID = userSnapshot.child("hwid").getValue(String.class);
                                System.out.println("🔹 Found HWID in database: " + storedHWID);

                                if (storedHWID == null || storedHWID.isEmpty()) {
                                    userSnapshot.getRef().child("hwid").setValueAsync(hwid);
                                    System.out.println("✅ HWID is now saved in Firebase!");
                                    valid = true;
                                } else if (storedHWID.equals(hwid)) {
                                    System.out.println("✅ HWID Matched! Access Granted.");
                                    valid = true;
                                } else {
                                    System.out.println("❌ HWID Mismatch! Access Denied.");
                                }
                                break;
                            }
                        }
                    }

                    boolean finalValid = valid;
                    SwingUtilities.invokeLater(() -> {
                        if (finalValid) {
                            onSuccess.run();
                        } else {
                            onFailure.run();
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("❌ Database Error: " + error.getMessage());
                    SwingUtilities.invokeLater(onFailure);
                }
            });
        });

        return true;
    }

    public static String getHWID() {
        String hwid = "UNKNOWN_HWID";
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Process process = Runtime.getRuntime().exec("wmic csproduct get UUID");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                reader.readLine();
                hwid = reader.readLine().trim();
            } else if (os.contains("mac")) {
                Process process = Runtime.getRuntime().exec("ioreg -rd1 -c IOPlatformExpertDevice");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("IOPlatformUUID")) {
                        hwid = line.split("\"")[3];
                        break;
                    }
                }
            } else if (os.contains("nux") || os.contains("nix")) {
                Process process = Runtime.getRuntime().exec("cat /var/lib/dbus/machine-id");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                hwid = reader.readLine().trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hwid;
    }
}