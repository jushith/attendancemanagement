package com.example.vivo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText studentNameEditText, studentEmailEditText, studentPasswordEditText, studentRollNumberEditText;
    private Spinner classSpinner;
    private LinearLayout courseCheckboxLayout;
    private Button saveStudentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        studentNameEditText = findViewById(R.id.studentNameEditText);
        studentEmailEditText = findViewById(R.id.studentEmailEditText);
        studentPasswordEditText = findViewById(R.id.studentPasswordEditText);
        studentRollNumberEditText = findViewById(R.id.studentRollNumberEditText);
        classSpinner = findViewById(R.id.classSpinner);
        courseCheckboxLayout = findViewById(R.id.courseCheckboxLayout);
        saveStudentButton = findViewById(R.id.saveStudentButton);

        // Load classes and courses
        loadClasses();
        loadCourses();

        saveStudentButton.setOnClickListener(v -> saveStudent());
    }

    private void loadClasses() {
        db.collection("Classes").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> classList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) { // Replaced var with DocumentSnapshot
                        classList.add(document.getString("className"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    classSpinner.setAdapter(adapter);

                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading classes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCourses() {
        db.collection("Courses").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) { // Replaced var with DocumentSnapshot
                        String courseName = document.getString("courseCode");
                        CheckBox checkBox = new CheckBox(this);
                        checkBox.setText(courseName);
                        courseCheckboxLayout.addView(checkBox);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveStudent() {
        String name = studentNameEditText.getText().toString().trim();
        String email = studentEmailEditText.getText().toString().trim();
        String password = studentPasswordEditText.getText().toString().trim();
        String rollNumber = studentRollNumberEditText.getText().toString().trim();
        String selectedClass = classSpinner.getSelectedItem() != null ? classSpinner.getSelectedItem().toString() : "";
        List<String> selectedCourses = new ArrayList<>();

        // Collect selected courses from the checkboxes
        for (int i = 0; i < courseCheckboxLayout.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) courseCheckboxLayout.getChildAt(i);
            if (checkBox.isChecked()) {
                selectedCourses.add(checkBox.getText().toString());
            }
        }

        // Validate input fields
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || rollNumber.isEmpty() || selectedClass.isEmpty() || selectedCourses.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select at least one course.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication requires a minimum password length of 6 characters
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare student data to save to Firestore
        Map<String, Object> studentData = new HashMap<>();
        studentData.put("name", name);
        studentData.put("email", email);
        studentData.put("password", password); // It's not recommended to store plain passwords
        studentData.put("rollNo", rollNumber);
        studentData.put("class", selectedClass);
        studentData.put("courses", selectedCourses);

        // Create a Firebase Authentication account for the student
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();

                    // Save the student data to Firestore
                    db.collection("Students").document(userId)
                            .set(studentData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Close the activity
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving student to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating student account in Authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

