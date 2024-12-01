package com.example.vivo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class FacultyDashboardActivity extends AppCompatActivity {

    private Button markAttendanceButton, editAttendanceButton, viewLowAttendanceButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_dashboard);

        markAttendanceButton = findViewById(R.id.markAttendanceButton);
        editAttendanceButton = findViewById(R.id.editAttendanceButton);
        viewLowAttendanceButton = findViewById(R.id.viewLowAttendanceButton);
        logoutButton = findViewById(R.id.logoutButton);

        markAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(FacultyDashboardActivity.this, FacultyAttendanceActivity.class);
            startActivity(intent);
        });

        editAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(FacultyDashboardActivity.this, EditAttendanceActivity.class);
            startActivity(intent);
        });

        viewLowAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(FacultyDashboardActivity.this, LowAttendanceActivity.class);
            startActivity(intent);
        });

        // Logout button click listener
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Sign out from Firebase Authentication
            Intent intent = new Intent(FacultyDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity stack
            startActivity(intent);
            finish(); // Finish the current activity
        });
    }
}
