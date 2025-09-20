package com.example.omg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            boolean isRegistered = sharedPreferences.getBoolean("isRegistered", false);

            if (isRegistered) {
                // Redirect to Login Page
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                // Redirect to Registration Page
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish(); // Close SplashScreen
        }, 2000); // 2-second delay
    }
}
