package com.example.vivo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Spinner courseSpinner;
    private TextView attendancePercentageText, attendanceDetailsText;

    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        courseSpinner = findViewById(R.id.courseSpinner);
        attendancePercentageText = findViewById(R.id.attendancePercentageText);
        attendanceDetailsText = findViewById(R.id.attendanceDetailsText);

        // Fetch current student's ID
        studentId = auth.getCurrentUser().getUid();

        // Load courses for the student
        loadCourses();

        // Handle course selection
        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = courseSpinner.getSelectedItem().toString();
                loadAttendanceDetails(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where no item is selected (optional)
            }
        });

    }

    private void loadCourses() {
        db.collection("Students")
                .document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> courses = (List<String>) documentSnapshot.get("courses");
                    if (courses != null && !courses.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        courseSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No courses found for this student.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAttendanceDetails(String selectedCourse) {
        db.collection("Attendance")
                .whereEqualTo("course", selectedCourse)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalClasses = queryDocumentSnapshots.size();
                    int attendedClasses = 0;
                    StringBuilder attendanceDetails = new StringBuilder();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> presentStudents = (List<String>) document.get("presentStudents");
                        String date = document.getString("date");

                        if (presentStudents != null && presentStudents.contains(studentId)) {
                            attendedClasses++;
                            attendanceDetails.append("Date: ").append(date).append(" - Present\n");
                        } else {
                            attendanceDetails.append("Date: ").append(date).append(" - Absent\n");
                        }
                    }

                    // Calculate and display attendance percentage
                    double percentage = totalClasses == 0 ? 0 : ((double) attendedClasses / totalClasses) * 100;
                    attendancePercentageText.setText(String.format("Attendance Percentage: %.2f%%", percentage));

                    // Display attendance details
                    attendanceDetailsText.setText("Attendance Details:\n" + attendanceDetails);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
