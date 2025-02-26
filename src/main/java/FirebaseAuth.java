import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class FirebaseAuth {
    private static DatabaseReference database;

    public static void initializeFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // ✅ Load JSON from inside the JAR (instead of using FileInputStream)
                InputStream serviceAccount = FirebaseAuth.class.getResourceAsStream("/memecoinserverauth.json");

                if (serviceAccount == null) {
                    throw new RuntimeException("❌ Firebase config file not found! Ensure 'memecoinserverauth.json' is inside 'src/main/resources'.");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://memecoin-auth-default-rtdb.europe-west1.firebasedatabase.app")
                        .build();

                FirebaseApp.initializeApp(options);
                database = FirebaseDatabase.getInstance().getReference("users");

                System.out.println("✅ Firebase initialized successfully!");

            } catch (Exception e) {
                System.err.println("❌ Error initializing Firebase: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Firebase already initialized.");
        }
    }

    public boolean verifyUserKey(String enteredKey) {
        if (database == null) {
            System.err.println("❌ Database is NULL. Cannot verify user key.");
            return false;
        }

        final boolean[] isValid = {false};
        CountDownLatch latch = new CountDownLatch(1);

        database.child("user1").child("key").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(Integer.class).equals(Integer.parseInt(enteredKey))) {
                    System.out.println("✅ Key Matched! Access Granted.");
                    isValid[0] = true;
                } else {
                    System.out.println("❌ Key does NOT match!");
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("❌ Database Error: " + error.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return isValid[0];
    }
}
