package com.example.vivo;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFacultyActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText facultyNameEditText, facultyEmailEditText, facultyPasswordEditText;
    private Spinner courseSpinner;
    private LinearLayout classCheckboxLayout;
    private Button saveFacultyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_faculty);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        facultyNameEditText = findViewById(R.id.facultyNameEditText);
        facultyEmailEditText = findViewById(R.id.facultyEmailEditText);
        facultyPasswordEditText = findViewById(R.id.facultyPasswordEditText);
        courseSpinner = findViewById(R.id.courseSpinner);
        classCheckboxLayout = findViewById(R.id.classCheckboxLayout);
        saveFacultyButton = findViewById(R.id.saveFacultyButton);

        // Load courses dynamically from Firestore
        loadCourses();

        // Load classes (hardcoded for now, can be made dynamic if required)
        loadClasses();

        // Save Faculty Button Click Listener
        saveFacultyButton.setOnClickListener(v -> saveFaculty());
    }

    private void loadCourses() {
        // Fetch courses from Firestore
        db.collection("Courses").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> courseList = new ArrayList<>();
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                courseList.add(document.getString("courseCode")); // Assume courseCode is the field name
            }

            if (courseList.isEmpty()) {
                Toast.makeText(this, "No courses available. Please add courses.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Populate spinner with courses
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            courseSpinner.setAdapter(adapter);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadClasses() {
        // Hardcoded classes for now; can fetch dynamically if needed
        String[] classes = {"Class A", "Class B", "Class C"};
        for (String className : classes) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(className);
            classCheckboxLayout.addView(checkBox);
        }
    }

    private void saveFaculty() {
        String name = facultyNameEditText.getText().toString().trim();
        String email = facultyEmailEditText.getText().toString().trim();
        String password = facultyPasswordEditText.getText().toString().trim();
        String course = (String) courseSpinner.getSelectedItem(); // Selected course from spinner
        List<String> selectedClasses = new ArrayList<>();

        // Collect selected classes
        for (int i = 0; i < classCheckboxLayout.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) classCheckboxLayout.getChildAt(i);
            if (checkBox.isChecked()) {
                selectedClasses.add(checkBox.getText().toString());
            }
        }

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || selectedClasses.isEmpty() || course == null) {
            Toast.makeText(this, "Please fill all fields and select at least one class.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save faculty data to Firestore and Firebase Authentication
        Map<String, Object> facultyData = new HashMap<>();
        facultyData.put("name", name);
        facultyData.put("email", email);
        facultyData.put("course", course);
        facultyData.put("classes", selectedClasses);
        facultyData.put("role", "faculty");

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid(); // Get the UID from Authentication

                    // Save faculty data to Firestore under the "Users" collection
                    db.collection("Users").document(userId)
                            .set(facultyData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Faculty added successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Close the activity
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving faculty to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating faculty account in Authentication: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
