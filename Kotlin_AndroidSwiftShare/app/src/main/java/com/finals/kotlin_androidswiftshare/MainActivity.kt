package com.finals.kotlin_androidswiftshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    /**
     * ON CREATE-- Most things initialize here.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /**
         * As of now, we only have this button for our whole functionality.
         * Might need to have a separate one for LAN based sending
         *
         * TODO: Settings Button (?), Send via LAN?
         * TODO (minor): Change fileSenderButton to bluetoothSenderButton, create localSenderButton for LAN
         */

        val fileSenderButton = findViewById<Button>(R.id.LocateFileBTN)

        fileSenderButton.setOnClickListener {
            // Launch FileSender activity
            val intent = Intent(this, FileSender::class.java)
            startActivity(intent)
        }
    }
}