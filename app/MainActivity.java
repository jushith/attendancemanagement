package com.example.vivo;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize Firebase Remote Config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Set Remote Config settings
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)  // Fetch interval set to 1 hour
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // Set default values for Remote Config parameters
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        // Fetch and apply Remote Config values
        fetchRemoteConfig();
    }

    private void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);

                            // Access and use the fetched values
                            String welcomeMessage = mFirebaseRemoteConfig.getString("welcome_message");
                            boolean isFeatureEnabled = mFirebaseRemoteConfig.getBoolean("feature_enabled");
                            String backgroundColor = mFirebaseRemoteConfig.getString("background_color");

                            // Display the welcome message in a TextView
                            TextView welcomeTextView = findViewById(R.id.welcomeTextView);
                            welcomeTextView.setText(welcomeMessage);

                            // Set background color if needed
                            welcomeTextView.setBackgroundColor(Color.parseColor(backgroundColor));
                            
                            // Use isFeatureEnabled to show/hide features as needed
                            if (isFeatureEnabled) {
                                // Show the feature
                            } else {
                                // Hide the feature
                            }
                        } else {
                            Log.e(TAG, "Fetch failed");
                        }
                    }
                });
    }
}
