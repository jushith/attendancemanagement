package com.example.vivo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCourseActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText courseCodeEditText, courseNameEditText;
    private Button saveCourseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        db = FirebaseFirestore.getInstance();

        courseCodeEditText = findViewById(R.id.courseCodeEditText);
        courseNameEditText = findViewById(R.id.courseNameEditText);
        saveCourseButton = findViewById(R.id.saveCourseButton);

        saveCourseButton.setOnClickListener(v -> saveCourse());
    }

    private void saveCourse() {
        String courseCode = courseCodeEditText.getText().toString().trim();
        String courseName = courseNameEditText.getText().toString().trim();

        if (courseCode.isEmpty() || courseName.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> courseData = new HashMap<>();
        courseData.put("courseCode", courseCode);
        courseData.put("courseName", courseName);

        db.collection("Courses").add(courseData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Course added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
