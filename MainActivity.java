package com.example.omg;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText name, contact, dob, email, id;
    Button insert, scanFaceBtn;
    MyDBHelper dbHelper;

    Bitmap scannedFaceBitmap = null;
    static final int REQUEST_IMAGE_CAPTURE = 101;
    static final int REQUEST_CAMERA_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.name);
        contact = findViewById(R.id.phone);
        dob = findViewById(R.id.dob);
        email = findViewById(R.id.mail);
        id = findViewById(R.id.voterid);
        insert = findViewById(R.id.start);
        scanFaceBtn = findViewById(R.id.scanFaceBtn);

        dbHelper = new MyDBHelper(this);

        scanFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermissionAndOpenCamera();
            }
        });

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s1 = name.getText().toString();
                String s2 = contact.getText().toString();
                String s3 = dob.getText().toString();
                String s4 = email.getText().toString();
                String s5 = id.getText().toString();

                if (s1.isEmpty() || s2.isEmpty() || s3.isEmpty() || s4.isEmpty() || s5.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (scannedFaceBitmap == null) {
                    Toast.makeText(MainActivity.this, "Please scan your face before registering", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] imageBytes = bitmapToByteArray(scannedFaceBitmap);

                boolean ins = dbHelper.insertuserdata(s5, s1, s2, s3, s4, imageBytes);
                if (ins) {
                    uploadFaceToServer(s5, scannedFaceBitmap);

                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isRegistered", true);
                    editor.apply();

                    Toast.makeText(MainActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ✅ Check permission before opening camera
    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    // ✅ Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to scan face", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            scannedFaceBitmap = (Bitmap) data.getExtras().get("data");
            Toast.makeText(this, "Face captured successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void uploadFaceToServer(String voterId, Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody voterIdPart = RequestBody.create(MultipartBody.FORM, voterId);
        RequestBody imagePart = RequestBody.create(MediaType.parse("image/*"), byteArray);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", voterId + ".jpg", imagePart);

        FaceAPIService apiService = ApiClient.getClient().create(FaceAPIService.class);
        Call<ResponseBody> call = apiService.registerFace(filePart, voterIdPart);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Face uploaded to server ✅", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Face upload failed ❌", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Error:","Check:"+t.getMessage());
            }
        });
    }
}
