package com.example.vivo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button addFacultyButton, addStudentButton, addClassButton, addCourseButton, logoutButton;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        addFacultyButton = findViewById(R.id.addFacultyButton);
        addStudentButton = findViewById(R.id.addStudentButton);
        addClassButton = findViewById(R.id.addClassButton);
        addCourseButton = findViewById(R.id.addCourseButton);
        logoutButton = findViewById(R.id.logoutButton);

        executorService = Executors.newSingleThreadExecutor();

        // Navigate to AddFacultyActivity
        addFacultyButton.setOnClickListener(v -> navigateToActivity(AddFacultyActivity.class));

        // Navigate to AddStudentActivity
        addStudentButton.setOnClickListener(v -> navigateToActivity(AddStudentActivity.class));

        // Navigate to AddClassActivity
        addClassButton.setOnClickListener(v -> navigateToActivity(AddClassActivity.class));

        // Navigate to AddCourseActivity
        addCourseButton.setOnClickListener(v -> navigateToActivity(AddCourseActivity.class));

        // Logout button to log out the admin
        logoutButton.setOnClickListener(v -> logout());
    }

    private void navigateToActivity(Class<?> targetActivity) {
        executorService.execute(() -> {
            runOnUiThread(() -> {
                Intent intent = new Intent(AdminDashboardActivity.this, targetActivity);
                startActivity(intent);
            });
        });
    }

    private void logout() {
        executorService.execute(() -> {
            FirebaseAuth.getInstance().signOut();
            runOnUiThread(() -> {
                Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
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
