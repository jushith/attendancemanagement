package com.example.vivo;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FacultyAttendanceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Spinner classSpinner;
    private LinearLayout studentCheckboxLayout;
    private Button saveAttendanceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_attendance);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        classSpinner = findViewById(R.id.classSpinner);
        studentCheckboxLayout = findViewById(R.id.studentCheckboxLayout);
        saveAttendanceButton = findViewById(R.id.saveAttendanceButton);

        // Load classes for logged-in faculty
        loadFacultyClasses();

        // Handle class selection
        classSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedClass = classSpinner.getSelectedItem().toString();
                loadStudentsForClass(selectedClass);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Save attendance
        saveAttendanceButton.setOnClickListener(v -> saveAttendance());
    }

    private void loadFacultyClasses() {
        String facultyId = auth.getCurrentUser().getUid(); // Get logged-in faculty's UID

        db.collection("Users").document(facultyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> classes = (List<String>) documentSnapshot.get("classes");
                    if (classes != null && !classes.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        classSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No classes assigned to you.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading classes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadStudentsForClass(String selectedClass) {
        studentCheckboxLayout.removeAllViews(); // Clear previous checkboxes

        db.collection("Students")
                .whereEqualTo("class", selectedClass) // Query for students in the selected class
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String studentName = document.getString("name");
                            if (studentName != null) {
                                CheckBox checkBox = new CheckBox(this);
                                checkBox.setText(studentName); // Use student name for display
                                checkBox.setTag(document.getId()); // Use student ID for identification
                                studentCheckboxLayout.addView(checkBox);
                            }
                        }
                    } else {
                        Toast.makeText(this, "No students found for the selected class.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveAttendance() {
        String selectedClass = classSpinner.getSelectedItem().toString(); // Get selected class
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); // Current date
        List<String> presentStudents = new ArrayList<>(); // List to store present student names

        // Collect present students
        for (int i = 0; i < studentCheckboxLayout.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) studentCheckboxLayout.getChildAt(i);
            if (checkBox.isChecked()) {
                String studentName = (String) checkBox.getText(); // Retrieve student name from CheckBox text
                if (studentName != null && !studentName.isEmpty()) { // Ensure valid name
                    presentStudents.add(studentName); // Add name to the list
                }
            }
        }

        if (presentStudents.isEmpty()) {
            Toast.makeText(this, "No students marked as present.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the course from the faculty's data
        String facultyId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Users").document(facultyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String assignedCourse = documentSnapshot.getString("course"); // Get course from faculty's data
                    if (assignedCourse != null) {
                        // Prepare attendance data with course, class, and present students
                        Map<String, Object> attendanceRecord = new HashMap<>();
                        attendanceRecord.put("class", selectedClass);
                        attendanceRecord.put("course", assignedCourse); // Include course
                        attendanceRecord.put("date", currentDate);
                        attendanceRecord.put("presentStudents", presentStudents);

                        // Use a unique document ID to avoid duplicates
                        String documentId = selectedClass + "_" + assignedCourse + "_" + currentDate;

                        db.collection("Attendance").document(documentId) // Save the attendance
                                .set(attendanceRecord)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Attendance saved successfully!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Error saving attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Error: No course assigned to this faculty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching faculty course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
