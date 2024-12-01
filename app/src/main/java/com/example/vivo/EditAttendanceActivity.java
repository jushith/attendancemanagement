package com.example.vivo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditAttendanceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner classSpinner;
    private LinearLayout studentCheckboxLayout;
    private Button selectDateButton, saveAttendanceButton;

    private String selectedDate;
    private String selectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attendance);

        db = FirebaseFirestore.getInstance();

        classSpinner = findViewById(R.id.classSpinner);
        studentCheckboxLayout = findViewById(R.id.studentCheckboxLayout);
        selectDateButton = findViewById(R.id.selectDateButton);
        saveAttendanceButton = findViewById(R.id.saveAttendanceButton);

        // Load classes assigned to the faculty
        loadClasses();

        // Listener for the "Select Date" button
        selectDateButton.setOnClickListener(v -> showDatePicker());

        // Listener for the "Save Attendance" button
        saveAttendanceButton.setOnClickListener(v -> saveAttendance());
    }

    private void loadClasses() {
        String facultyId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(facultyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> classes = (List<String>) documentSnapshot.get("classes");
                    if (classes != null && !classes.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        classSpinner.setAdapter(adapter);

                        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedClass = classes.get(position);
                                if (selectedDate != null) {
                                    loadAttendanceForClassAndDate(selectedClass, selectedDate);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selectedClass = null;
                            }
                        });
                    } else {
                        Toast.makeText(this, "No classes assigned to you.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading classes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    selectDateButton.setText(selectedDate);
                    if (selectedClass != null) {
                        loadAttendanceForClassAndDate(selectedClass, selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadAttendanceForClassAndDate(String selectedClass, String selectedDate) {
        studentCheckboxLayout.removeAllViews(); // Clear existing checkboxes

        // Fetch students in the selected class
        db.collection("Students")
                .whereEqualTo("class", selectedClass)
                .get()
                .addOnSuccessListener(studentSnapshots -> {
                    if (!studentSnapshots.isEmpty()) {
                        // Add checkboxes for each student in the class
                        for (DocumentSnapshot studentDoc : studentSnapshots) {
                            String studentName = studentDoc.getString("name");
                            String studentId = studentDoc.getId();

                            CheckBox checkBox = new CheckBox(this);
                            checkBox.setText(studentName); // Display student name
                            checkBox.setTag(studentId); // Tag with student ID for identification
                            studentCheckboxLayout.addView(checkBox);
                        }

                        // Fetch attendance records for the selected class and date
                        db.collection("Attendance")
                                .whereEqualTo("class", selectedClass)
                                .whereEqualTo("date", selectedDate)
                                .get()
                                .addOnSuccessListener(attendanceSnapshots -> {
                                    if (!attendanceSnapshots.isEmpty()) {
                                        DocumentSnapshot attendanceDoc = attendanceSnapshots.getDocuments().get(0);
                                        List<String> presentStudents = (List<String>) attendanceDoc.get("presentStudents");

                                        // Update checkboxes to reflect attendance
                                        for (int i = 0; i < studentCheckboxLayout.getChildCount(); i++) {
                                            CheckBox checkBox = (CheckBox) studentCheckboxLayout.getChildAt(i);
                                            String studentName = checkBox.getText().toString(); // Get student name
                                            if (presentStudents != null && presentStudents.contains(studentName)) {
                                                checkBox.setChecked(true);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "No attendance records found for the selected date.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error fetching attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No students found for the selected class.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveAttendance() {
        if (selectedClass == null || selectedDate == null) {
            Toast.makeText(this, "Please select both a class and a date.", Toast.LENGTH_SHORT).show();
            return;
        }

        String facultyId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(facultyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String associatedCourse = documentSnapshot.getString("course");

                    if (associatedCourse == null || associatedCourse.isEmpty()) {
                        Toast.makeText(this, "Error: No course assigned to you.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String documentId = selectedClass + "_" + associatedCourse + "_" + selectedDate;

                    // Fetch the existing attendance document
                    db.collection("Attendance").document(documentId)
                            .get()
                            .addOnSuccessListener(attendanceSnapshot -> {
                                List<String> currentPresentStudents = new ArrayList<>();
                                if (attendanceSnapshot.exists()) {
                                    currentPresentStudents = (List<String>) attendanceSnapshot.get("presentStudents");
                                    if (currentPresentStudents == null) currentPresentStudents = new ArrayList<>();
                                }

                                // Update attendance based on checkbox states
                                List<String> updatedPresentStudents = new ArrayList<>();
                                final int childCount = studentCheckboxLayout.getChildCount();

                                for (int i = 0; i < childCount; i++) {
                                    final int index = i; // Declare as final to use in the lambda
                                    CheckBox checkBox = (CheckBox) studentCheckboxLayout.getChildAt(index);
                                    String userId = (String) checkBox.getTag();

                                    if (checkBox.isChecked()) {
                                        // Fetch student name using the userId
                                        db.collection("Students").document(userId).get()
                                                .addOnSuccessListener(studentSnapshot -> {
                                                    String studentName = studentSnapshot.getString("name");
                                                    if (studentName != null && !updatedPresentStudents.contains(studentName)) {
                                                        updatedPresentStudents.add(studentName);
                                                    }

                                                    // Save attendance only after processing the last checkbox
                                                    if (index == childCount - 1) {
                                                        saveEditedAttendance(documentId, selectedClass, associatedCourse, selectedDate, updatedPresentStudents);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Error fetching student details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else if (index == childCount - 1) {
                                        // Handle the case where no students are checked
                                        saveEditedAttendance(documentId, selectedClass, associatedCourse, selectedDate, updatedPresentStudents);
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error fetching attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading faculty course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveEditedAttendance(String documentId, String selectedClass, String associatedCourse, String selectedDate, List<String> updatedPresentStudents) {
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("class", selectedClass);
        attendanceData.put("course", associatedCourse);
        attendanceData.put("date", selectedDate);
        attendanceData.put("presentStudents", updatedPresentStudents);

        db.collection("Attendance").document(documentId)
                .set(attendanceData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Attendance updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
