package com.example.main_androidswiftshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Button btnSend, btnReceive, btnFileLocator;

    /** TODO: ITERATION 01 - INITIAL PLACEHOLDER FEATURES (NEAR-FUNCTIONAL) - DEADLINE: MARCH 14, 2025
     * 1.)  REQUEST user to allow LOCAL file access. Written already as manifest uses-permissions.
     *      Need to actually make a POP-UP request with yes/no response.
     * 2.)  INITIAL button features.
     *      - Projected Features:
     *          --
     *          openFileBTN         IF USER did NOT allow local file permissions. Request.
     *                              ELSE: Continue to file explorer.
     *          --
     *          sendFileBTN         IF USER did NOT allow local file permissions. Request.
     *                              ELSE: Go to file explorer view (to pick where to send)
     *          --
     *          receiveFileBTN      REQUEST bluetooth-usage "Allow this app to access bluetooth?" Y/N
     *                              IF USER picked NO, retry. Go back to home.
     *                              IF USER did NOT allow local file permissions. Request.
     *                              ELSE: Go to File Explorer / Receive Class.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSend = findViewById(R.id.sendBTN);
        btnReceive = findViewById(R.id.receiveBTN);
        btnFileLocator = findViewById(R.id.filelocatorBTN);


        btnSend.setOnClickListener(v -> {
            // TODO: Add logic for sendFileBTN
        });

        btnReceive.setOnClickListener(v -> {
            // TODO: Add logic for receiveFileBTN
        });

        btnFileLocator.setOnClickListener(v -> {

            // TODO: Add logic for openFileBTN

            Intent intent = new Intent(MainActivity.this, FileLocator.class);
            startActivity(intent);
            // Optionally, hide or finish the current MainActivity
            MainActivity.this.finish();

        });
    }
}
