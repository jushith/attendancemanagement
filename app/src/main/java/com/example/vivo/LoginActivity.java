package com.example.vivo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText emailInput, passwordInput;
    private Button loginButton;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        executorService = Executors.newSingleThreadExecutor();

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(email, password);
        });
    }

    private void performLogin(String email, String password) {
        executorService.execute(() -> {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            navigateToDashboard(user.getUid());
                        }
                    })
                    .addOnFailureListener(e -> runOnUiThread(() ->
                            Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    ));
        });
    }

    private void navigateToDashboard(String userId) {
        Log.d("LoginActivity", "Navigating for user ID: " + userId);

        executorService.execute(() -> {
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d("LoginActivity", "Document data: " + documentSnapshot.getData());
                            String role = documentSnapshot.getString("role");
                            if (role == null) {
                                runOnUiThread(() ->
                                        Toast.makeText(this, "Error: User role not found.", Toast.LENGTH_SHORT).show()
                                );
                                return;
                            }

                            Intent intent;
                            switch (role) {
                                case "admin":
                                    intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                    break;
                                case "faculty":
                                    intent = new Intent(LoginActivity.this, FacultyDashboardActivity.class);
                                    break;
                                case "student":
                                    intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                                    break;
                                default:
                                    runOnUiThread(() ->
                                            Toast.makeText(this, "Invalid role.", Toast.LENGTH_SHORT).show()
                                    );
                                    return;
                            }

                            runOnUiThread(() -> {
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            // Create the user document if it doesn't exist
                            Log.d("LoginActivity", "User document not found. Creating default document.");
                            createDefaultUserDocument(userId);
                        }
                    })
                    .addOnFailureListener(e -> runOnUiThread(() ->
                            Toast.makeText(this, "Error retrieving user role: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    ));
        });
    }

    private void createDefaultUserDocument(String userId) {
        Map<String, Object> defaultUser = new HashMap<>();
        defaultUser.put("email", auth.getCurrentUser().getEmail());
        defaultUser.put("role", "student"); // Default role, change as needed

        executorService.execute(() -> {
            db.collection("Users").document(userId).set(defaultUser)
                    .addOnSuccessListener(aVoid -> {
                        runOnUiThread(() ->
                                Toast.makeText(this, "User document created successfully.", Toast.LENGTH_SHORT).show()
                        );
                        navigateToDashboard(userId); // Retry navigation after creating document
                    })
                    .addOnFailureListener(e -> runOnUiThread(() ->
                            Toast.makeText(this, "Error creating user document: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    ));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
