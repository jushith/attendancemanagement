package com.example.vivo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LowAttendanceActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private Spinner courseSpinner, classSpinner;
    private TextView lowAttendanceList;

    private String selectedCourse;
    private String selectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_attendance);

        db = FirebaseFirestore.getInstance();

        courseSpinner = findViewById(R.id.courseSpinner);
        classSpinner = findViewById(R.id.classSpinner);
        lowAttendanceList = findViewById(R.id.lowAttendanceList);

        // Load courses
        loadCourses();

        // Handle course selection
        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = courseSpinner.getSelectedItem().toString();
                loadClasses(); // Load classes based on the selected course
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle class selection
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedClass = classSpinner.getSelectedItem().toString();
                fetchLowAttendanceStudents(); // Fetch attendance details based on selected class
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadCourses() {
        db.collection("Courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> courses = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        courses.add(document.getString("courseCode"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    courseSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading courses: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadClasses() {
        if (selectedCourse == null || selectedCourse.isEmpty()) {
            Toast.makeText(this, "Please select a course.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Classes") // Fetch from the Classes collection
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> classes = new ArrayList<>();
                    for (DocumentSnapshot classDoc : queryDocumentSnapshots) {
                        String className = classDoc.getString("className");
                        if (className != null && !classes.contains(className)) {
                            classes.add(className); // Add unique class names
                        }
                    }

                    if (!classes.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        classSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No classes found for the selected course.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading classes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void fetchLowAttendanceStudents() {
        if (selectedCourse == null || selectedClass == null) {
            Toast.makeText(this, "Please select a course and class.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log selected class and course
        System.out.println("Selected Class: " + selectedClass);
        System.out.println("Selected Course: " + selectedCourse);

        // Fetch students belonging to the selected class and course
        db.collection("Students")
                .whereEqualTo("class", selectedClass) // Use whereEqualTo for class
                .whereArrayContains("courses", selectedCourse) // Use whereArrayContains for courses
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No students found for the selected class and course.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Map<String, Object>> studentAttendanceData = new ArrayList<>();
                    for (DocumentSnapshot studentDoc : queryDocumentSnapshots) {
                        Map<String, Object> studentData = new HashMap<>();
                        String studentName = studentDoc.getString("name");
                        studentData.put("name", studentName); // Add student name to the list
                        studentAttendanceData.add(studentData);
                    }

                    calculateAttendancePercentage(studentAttendanceData);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void calculateAttendancePercentage(List<Map<String, Object>> studentList) {
        db.collection("Attendance")
                .whereEqualTo("class", selectedClass)
                .whereEqualTo("course", selectedCourse)
                .get()
                .addOnSuccessListener(attendanceSnapshots -> {
                    int totalClasses = attendanceSnapshots.size(); // Total classes for the selected class and course
                    Map<String, Integer> attendanceCounts = new HashMap<>();

                    for (DocumentSnapshot attendanceDoc : attendanceSnapshots) {
                        List<String> presentStudents = (List<String>) attendanceDoc.get("presentStudents");
                        if (presentStudents != null) {
                            for (String studentName : presentStudents) {
                                attendanceCounts.put(studentName, attendanceCounts.getOrDefault(studentName, 0) + 1);
                            }
                        }
                    }

                    StringBuilder lowAttendanceDetails = new StringBuilder();
                    for (Map<String, Object> studentData : studentList) {
                        String studentName = (String) studentData.get("name");
                        int attendedClasses = attendanceCounts.getOrDefault(studentName, 0);
                        double attendancePercentage = totalClasses > 0 ? (attendedClasses / (double) totalClasses) * 100 : 0;

                        if (attendancePercentage < 75) {
                            lowAttendanceDetails.append(studentName)
                                    .append(" - ")
                                    .append(String.format(Locale.getDefault(), "%.2f%%", attendancePercentage))
                                    .append("\n");
                        }
                    }

                    if (lowAttendanceDetails.length() == 0) {
                        lowAttendanceList.setText("All students have attendance above 75%.");
                    } else {
                        lowAttendanceList.setText("Students with Attendance Below 75%:\n" + lowAttendanceDetails);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching attendance records: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
