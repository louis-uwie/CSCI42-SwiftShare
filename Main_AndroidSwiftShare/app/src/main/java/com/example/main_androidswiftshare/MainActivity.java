package com.example.main_androidswiftshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button btnSend, btnReceive, btnFileLocator;
    private static final int STORAGE_PERMISSION_CODE = 101; // Photo/Video

    private static final int FILE_PICKER_REQUEST_CODE = 102; // Local File



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = findViewById(R.id.sendBTN);
        btnReceive = findViewById(R.id.receiveBTN);
        btnFileLocator = findViewById(R.id.filelocatorBTN);

        checkPermissionOnStartup();

        btnFileLocator.setOnClickListener(v -> {
            if (hasStoragePermission()) {
                openFilePicker();
                finish();
            } else {
                requestStoragePermission();
            }
        });

        btnSend.setOnClickListener(v -> {
            /** TODO: Create SEND button logic
             *  Ask for Bluetooth Permission
             *  If Allowed: Go to DeviceLocator
             *  Send File Soon.
             */
        });

        btnReceive.setOnClickListener(v -> {
            /** TODO: Create RECEIVE button Logic
             *  Ask for Bluetooth Permission
             *  Allow other users to scan for this device
             *  Receive file.
             *  -
             *  Suggestion: Have a dedicated ~/SwiftShare directory?
             */


        });
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(this, "Storage access is required for managing files.", Toast.LENGTH_LONG).show();
        }

        ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
    }

    private void checkPermissionOnStartup() {
        if (!hasStoragePermission()) {
            requestStoragePermission();
        } else {
            Toast.makeText(this, "Storage permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");  // All file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a File"), FILE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();

            if (fileUri != null) {
                Toast.makeText(this, "File selected: " + fileUri.getPath(), Toast.LENGTH_SHORT).show();

                // Launch FileLocator Activity after selecting the file
                Intent intent = new Intent(MainActivity.this, FileLocator.class);
                intent.putExtra("selected_file_uri", fileUri.toString());
                startActivity(intent);
                finish(); // Close MainActivity if needed
            } else {
                Toast.makeText(this, "No file selected.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
