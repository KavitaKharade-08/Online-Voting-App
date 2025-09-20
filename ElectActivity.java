package com.example.omg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ElectActivity extends AppCompatActivity {

    Button voteButton1, voteButton2, voteButton3;
    String voterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elect); // Ensure this matches your layout filename

        voterId = getIntent().getStringExtra("voterId");

        voteButton1 = findViewById(R.id.voteButton1);
        voteButton2 = findViewById(R.id.voteButton2);
        voteButton3 = findViewById(R.id.voteButton3);

        voteButton1.setOnClickListener(view -> showVoteConfirmationDialog("BJP"));
        voteButton2.setOnClickListener(view -> showVoteConfirmationDialog("Congress"));
        voteButton3.setOnClickListener(view -> showVoteConfirmationDialog("Aam Aadmi Party"));
    }

    private void showVoteConfirmationDialog(String partyName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ElectActivity.this);
        builder.setTitle("Confirm Your Vote");
        builder.setMessage("Are you sure you want to vote for " + partyName + "?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            // Navigate to Thank You page or next activity
            Intent intent = new Intent(ElectActivity.this, OTPGeneration.class); // Change to your target activity
            intent.putExtra("voterId",voterId);
            intent.putExtra("VOTED_PARTY", partyName); // Optional: pass data to next activity
            startActivity(intent);
            finish(); // Optional: prevent back navigation
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
