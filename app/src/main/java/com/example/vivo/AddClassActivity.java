package com.example.vivo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddClassActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText classNameEditText;
    private Button saveClassButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        db = FirebaseFirestore.getInstance();

        classNameEditText = findViewById(R.id.classNameEditText);
        saveClassButton = findViewById(R.id.saveClassButton);

        saveClassButton.setOnClickListener(v -> saveClass());
    }

    private void saveClass() {
        String className = classNameEditText.getText().toString().trim();

        if (className.isEmpty()) {
            Toast.makeText(this, "Class name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> classData = new HashMap<>();
        classData.put("className", className);

        db.collection("Classes").add(classData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Class added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
