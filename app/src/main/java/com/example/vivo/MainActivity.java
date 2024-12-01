package com.example.vivo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Fetch user role
            fetchUserRole(currentUser.getEmail());
        } else {
            // Navigate to LoginActivity
            navigateToLogin();
        }
    }

    private void fetchUserRole(String email) {
        db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String role = document.getString("role");
                        navigateToRoleDashboard(role);
                    } else {
                        Log.e("MainActivity", "No role found for user: " + email);
                        Toast.makeText(this, "Role not found. Please contact admin.", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error fetching user role", e);
                    Toast.makeText(this, "Error fetching user role", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
    }

    private void navigateToRoleDashboard(String role) {
        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("faculty".equalsIgnoreCase(role)) {
            intent = new Intent(this, FacultyDashboardActivity.class);
        } else if ("student".equalsIgnoreCase(role)) {
            intent = new Intent(this, StudentAttendanceActivity.class);
        } else {
            Toast.makeText(this, "Invalid role. Please contact admin.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        startActivity(intent);
        finish(); // Prevent returning to this screen
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
