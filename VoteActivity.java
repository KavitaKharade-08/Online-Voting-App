package com.example.omg;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class VoteActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean isFaceCaptured = false;
    private Bitmap capturedBitmap = null;
    private Button btnVerifyFace;
    private String voterId;
    private static final String MATCH_URL =  "http://192.168.203.212:5000/match_face";

    private FaceDetector detector;
    private ProgressDialog dialog;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        voterId = getIntent().getStringExtra("voter_id");
        Log.e("VoteActivity", "Voter ID: " + voterId);

        surfaceView = findViewById(R.id.surfaceCamera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        btnVerifyFace = findViewById(R.id.btnVerifyFace);
        btnVerifyFace.setEnabled(false);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .enableTracking()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        detector = FaceDetection.getClient(options);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Verifying face...");
        dialog.setCancelable(false);

        requestQueue = Volley.newRequestQueue(this);

        btnVerifyFace.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                dialog.show();
                sendFaceToServer(capturedBitmap);
            } else {
                Toast.makeText(this, "No face captured!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        try {
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (Exception e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isFaceCaptured) return;

        Camera.Size size = camera.getParameters().getPreviewSize();
        Bitmap bitmap = decodeToBitmap(data, size.width, size.height);

        if (bitmap == null) return;

        InputImage image = InputImage.fromBitmap(bitmap, 270);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        Face face = faces.get(0);
                        Float smile = face.getSmilingProbability();
                        Float rightEye = face.getRightEyeOpenProbability();
                        Float leftEye = face.getLeftEyeOpenProbability();

                        if (smile != null && rightEye != null && leftEye != null &&
                                smile > 0.1 && rightEye > 0.1 && leftEye > 0.1) {

                            if (!isFaceCaptured) {  // Make sure this block runs only once
                                isFaceCaptured = true; // Mark that the face has been captured
                                capturedBitmap = bitmap;
                                btnVerifyFace.setEnabled(true);
                                Toast.makeText(this, "Liveness Verified. Tap Verify Face.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FaceDetection", "Detection failed: " + e.getMessage()));
    }

    private Bitmap decodeToBitmap(byte[] data, int width, int height) {
        try {
            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            YuvImage yuv = new YuvImage(data, camera.getParameters().getPreviewFormat(),
                    previewSize.width, previewSize.height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 90, out);
            byte[] bytes = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return Bitmap.createScaledBitmap(bitmap, 640, 480, true);
        } catch (Exception e) {
            Log.e("BitmapError", "Decode failed: " + e.getMessage());
            return null;
        }
    }

    private void sendFaceToServer(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        String base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        StringRequest request = new StringRequest(Request.Method.POST, MATCH_URL,
                response -> {
                    dialog.dismiss();  // ✅ Dismiss the loader
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean verified = jsonResponse.getBoolean("verified");

                        if (verified) {
                            // Face match success
                            Toast.makeText(getApplicationContext(), "✅ Face matched", Toast.LENGTH_SHORT).show();
                            // Proceed to voting
                            Intent intent = new Intent(VoteActivity.this, ElectActivity.class);
                            intent.putExtra("voterId", voterId); // Replace with actual voter ID
                            startActivity(intent);

                            finish();
                        } else {
                            // Face mismatch
                            Toast.makeText(getApplicationContext(), "❌ Face did not match", Toast.LENGTH_SHORT).show();

                            resetFaceCapture();
                            // Optional: Retry or go back
                        }
                    } catch (JSONException e) {
                        dialog.dismiss();  // ✅ Dismiss the loader
                        e.printStackTrace();
                        // Handle JSON parsing error properly
                        Toast.makeText(getApplicationContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle network/Volley errors
                    dialog.dismiss();
                    Log.e("VolleyError", error.toString());  // Log the error message for debugging
                    Toast.makeText(getApplicationContext(), "Server error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("voter_id", voterId);
                params.put("image", base64Image);
                return params;
            }
        };

        requestQueue.add(request);
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void resetFaceCapture() {
        // Reset the capture state
        isFaceCaptured = false;  // Allow the face to be captured again
        capturedBitmap = null;   // Clear the captured image

        btnVerifyFace.setEnabled(true);  // Disable the "Verify Face" button until face is captured again

        // Optionally, you can restart camera preview or show other UI elements if needed
        // If needed, restart the camera preview (this may not be necessary for your app flow)
        if (camera != null) {
            camera.startPreview();  // Restart preview to continue detecting faces
        }
    }
}

