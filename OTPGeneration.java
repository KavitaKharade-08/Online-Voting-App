package com.example.omg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.Random;

public class OTPGeneration extends AppCompatActivity {

    TextView registeredNumberText;
    ProgressBar progressBar;
    Button proceedButton;
    EditText userOtpInput;

    MyDBHelper dbHelper;
    String voterId, votedParty;
    String mobileNumber;
    String otp;

    private static final int SMS_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpgeneration);

        registeredNumberText = findViewById(R.id.registeredNumber);
        progressBar = findViewById(R.id.progressBar);
        proceedButton = findViewById(R.id.verifyOtpButton);
        userOtpInput = findViewById(R.id.otpInput);

        proceedButton.setVisibility(View.GONE);
        dbHelper = new MyDBHelper(this);

        voterId = getIntent().getStringExtra("voterId");
        votedParty = getIntent().getStringExtra("VOTED_PARTY");

        Log.d("OTPGeneration", "VoterId: " + voterId);
        Log.d("OTPGeneration", "VotedParty: " + votedParty);

        if (voterId != null) {
            mobileNumber = dbHelper.getMobileNumber(voterId);
            Log.d("OTPGeneration", "Fetched Mobile Number: " + mobileNumber);

            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                String maskedNumber = "•••• •••• " + mobileNumber.substring(mobileNumber.length() - 4);
                registeredNumberText.setText(maskedNumber);

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                } else {
                    sendOtp();
                }
            } else {
                Toast.makeText(this, "Mobile number not found!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid Voter ID!", Toast.LENGTH_SHORT).show();
        }

        proceedButton.setOnClickListener(v -> {
            String enteredOtp = userOtpInput.getText().toString().trim();
            if (enteredOtp.equals(otp)) {
                boolean inserted = dbHelper.insertVoteRecord(voterId, votedParty);

                if (inserted) {
                    Intent intent = new Intent(OTPGeneration.this, Final.class);
                    int count = dbHelper.getTotalVoterCount();
                    intent.putExtra("voterId", voterId);
                    intent.putExtra("totalVoteCount", count);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(this, "Failed to record vote!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Incorrect OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOtp() {
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (tm.getSimState() != TelephonyManager.SIM_STATE_READY) {
            Toast.makeText(this, "SIM not ready or missing", Toast.LENGTH_LONG).show();
            return;
        }

        otp = generateOtp();
        String message = "Dear " + voterId + ", your OTP is: " + otp + ". Do not share it with anyone.";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            if (!mobileNumber.startsWith("+")) {
                mobileNumber = "+91" + mobileNumber;
            }
            smsManager.sendTextMessage(mobileNumber, null, message, null, null);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "OTP sent to " + mobileNumber, Toast.LENGTH_LONG).show();
            proceedButton.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "OTP sending failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6-digit OTP
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendOtp();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
