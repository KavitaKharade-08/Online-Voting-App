package com.example.omg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText email, voterid;
    MyDBHelper dbHelper;
    Button loginButton;
    TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.e1);
        voterid = findViewById(R.id.e2);  // Initialized voterid

        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        dbHelper = new MyDBHelper(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = email.getText().toString().trim();
                String enteredVoterId = voterid.getText().toString().trim();

                // Check if email or voter ID is empty
                if (enteredEmail.isEmpty() || enteredVoterId.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if user exists in the Voter table
                boolean isUserValid = dbHelper.checkUser(enteredEmail, enteredVoterId);

                if (isUserValid) {
                    // Check if the user has already voted
                    boolean hasVoted = dbHelper.hasVoted(enteredVoterId);

                    if (hasVoted) {
                        // If user has already voted, go directly to the Final page
                        Toast.makeText(LoginActivity.this, "You have already voted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, Final.class);
                        intent.putExtra("voterId", enteredVoterId);  // Pass the voter ID to Final page
                        startActivity(intent);
                        finish();
                    } else {
                        // User has not voted yet, continue with normal flow
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.putExtra("voter_id", enteredVoterId);  // Pass voter ID to HomeActivity
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        });
    }
}
