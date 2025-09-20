package com.example.omg;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    TextView welcomeMsg;
    Button voteNowButton, knowMoreButton;
    String voterId;
    MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new MyDBHelper(this);

        welcomeMsg = findViewById(R.id.welcomeMsg);
        voteNowButton = findViewById(R.id.voteNowButton);
        knowMoreButton = findViewById(R.id.knowMoreButton);

        // Get voter ID from intent
        voterId = getIntent().getStringExtra("voter_id");
       // Toast.makeText(this, "Voter ID: " + voterId, Toast.LENGTH_LONG).show();

        if (voterId == null || voterId.isEmpty()) {
            Toast.makeText(this, "Voter ID not found!", Toast.LENGTH_SHORT).show();
            finish();
        }

        showVoterName();

        voteNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, VoteActivity.class);
            intent.putExtra("voter_id", voterId);
            startActivity(intent);
        });

        knowMoreButton.setOnClickListener(v -> {
            String url = "https://www.eci.gov.in";  // Official Election Commission of India
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });
    }

    private void showVoterName() {
        Cursor cursor = dbHelper.getVoterById(voterId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            welcomeMsg.setText("Welcome, " + name + "!");
            cursor.close();
        } else {
            welcomeMsg.setText("Welcome!");
        }
    }
}
