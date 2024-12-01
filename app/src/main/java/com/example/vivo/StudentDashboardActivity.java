package com.example.vivo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView attendanceSummary;
    private Spinner courseSpinner;
    private Button logoutButton;

    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        attendanceSummary = findViewById(R.id.attendanceSummary);
        courseSpinner = findViewById(R.id.courseSpinner);
        logoutButton = findViewById(R.id.logoutButton);

        // Get the current student ID
        studentId = auth.getCurrentUser().getUid();

        // Load student's courses and attendance
        loadStudentCourses();

        // Set Logout button listener
        logoutButton.setOnClickListener(v -> {
            auth.signOut(); // Sign out from Firebase Authentication
            Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the activity stack
            startActivity(intent);
            finish(); // Finish the current activity
        });
    }

    private void loadStudentCourses() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Fetch email of the logged-in user

        db.collection("Students").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        List<String> enrolledCourses = new ArrayList<>();

                        // Handle the 'courses' field as either a String or List
                        Object coursesObject = documentSnapshot.get("courses");
                        if (coursesObject instanceof String) {
                            // If courses is a single string, add it to the list
                            enrolledCourses.add((String) coursesObject);
                        } else if (coursesObject instanceof List) {
                            // If courses is a list, cast it directly
                            enrolledCourses.addAll((List<String>) coursesObject);
                        }

                        if (!enrolledCourses.isEmpty()) {
                            Log.d("StudentDashboard", "Enrolled courses: " + enrolledCourses);

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, enrolledCourses);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            courseSpinner.setAdapter(adapter);

                            // Set listener for course selection
                            courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedCourse = enrolledCourses.get(position);
                                    Log.d("StudentDashboard", "Selected course: " + selectedCourse);
                                    fetchAttendanceDetails(selectedCourse);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });

                        } else {
                            Toast.makeText(this, "No courses found for the student.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Student record not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading student data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchAttendanceDetails(String selectedCourse) {
        String loggedInStudentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("Students").whereEqualTo("email", loggedInStudentEmail).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot studentDoc = querySnapshot.getDocuments().get(0);
                        String studentName = studentDoc.getString("name"); // Fetch student name

                        if (studentName == null || studentName.isEmpty()) {
                            attendanceSummary.setText("Error: Student name not found in the database.");
                            return;
                        }

                        // Query the Attendance collection for the selected course
                        db.collection("Attendance")
                                .whereEqualTo("course", selectedCourse) // Match the selected course
                                .get()
                                .addOnSuccessListener(attendanceSnapshots -> {
                                    if (!attendanceSnapshots.isEmpty()) {
                                        int totalClasses = attendanceSnapshots.size();
                                        int attendedClasses = 0;
                                        StringBuilder attendanceDetails = new StringBuilder();

                                        for (DocumentSnapshot attendanceDoc : attendanceSnapshots) {
                                            List<String> presentStudents = (List<String>) attendanceDoc.get("presentStudents");
                                            if (presentStudents != null && presentStudents.contains(studentName)) {
                                                attendedClasses++;
                                                String date = attendanceDoc.getString("date");
                                                attendanceDetails.append("Date: ").append(date).append("\n");
                                            }
                                        }

                                        double attendancePercentage = (attendedClasses / (double) totalClasses) * 100;

                                        String summary = String.format(Locale.getDefault(),
                                                "Total Classes: %d\nAttended: %d\nAttendance Percentage: %.2f%%",
                                                totalClasses, attendedClasses, attendancePercentage);

                                        attendanceSummary.setText(summary + "\n\n" + attendanceDetails.toString());
                                    } else {
                                        attendanceSummary.setText("No attendance records found for this course.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error fetching attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        attendanceSummary.setText("Student record not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching student details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
