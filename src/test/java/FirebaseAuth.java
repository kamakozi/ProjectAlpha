import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

class FirebaseAuth {
    private DatabaseReference database;

    public FirebaseAuth() {
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://memecoin-auth-default-rtdb.europe-west1.firebasedatabase.app") // Your Firebase URL
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized successfully!");
            } else {
                System.out.println("⚠️ Firebase already initialized.");
            }

            database = FirebaseDatabase.getInstance().getReference("users");

        } catch (IOException e) {
            System.err.println("❌ Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }

        if (database == null) {
            System.err.println("❌ Database reference is STILL NULL! Firebase failed.");
        } else {
            System.out.println("✅ Database reference initialized successfully.");
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
                if (snapshot.exists()) {
                    Integer storedKey = snapshot.getValue(Integer.class);
                    System.out.println("🔍 Retrieved from Firebase: " + storedKey);

                    if (storedKey != null && storedKey.equals(Integer.parseInt(enteredKey))) {
                        System.out.println("✅ Key Matched! Access Granted.");
                        isValid[0] = true;
                    } else {
                        System.out.println("❌ Key does NOT match!");
                    }
                } else {
                    System.out.println("❌ No key found in Firebase!");
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
