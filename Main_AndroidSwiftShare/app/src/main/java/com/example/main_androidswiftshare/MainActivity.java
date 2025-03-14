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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button btnSend, btnReceive, btnFileLocator;
    private static final int STORAGE_PERMISSION_CODE = 101; // Photo/Video

    private static final int FILE_PICKER_REQUEST_CODE = 102; // Local File



    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_BT = 2;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private ArrayList<String> devicesList = new ArrayList<>();
    private LinearLayout bluetoothSubmenu;
    private ListView bluetoothDeviceList;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceInfo = (device.getName() != null ? device.getName() : "Unknown") + " - " + device.getAddress();
                    if (!devicesList.contains(deviceInfo)) {
                        devicesList.add(deviceInfo);
                        devicesAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
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

            Toast.makeText(this, "Send Button Clicked! This Feature is coming soon, Apologies for the wait.", Toast.LENGTH_LONG).show();
        });

        btnReceive.setOnClickListener(v -> {
            /** TODO: Create RECEIVE button Logic
             *  Ask for Bluetooth Permission
             *  Allow other users to scan for this device
             *  Receive file.
             *  -
             *  Suggestion: Have a dedicated ~/SwiftShare directory?
             */

            Toast.makeText(this, "Receive Button Clicked! This Feature is coming soon, Apologies for the wait.", Toast.LENGTH_LONG).show();


        });

        Button listDevicesButton = findViewById(R.id.listDevices);
        bluetoothSubmenu = findViewById(R.id.bluetoothSubmenu);
        Button closeSubmenu = findViewById(R.id.closeSubmenu);
        bluetoothDeviceList = findViewById(R.id.bluetoothDeviceList);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show();
            return;
        }

        listDevicesButton.setOnClickListener(v -> {
            bluetoothSubmenu.setVisibility(View.VISIBLE);
            checkBluetoothPermissionsAndScan();
        });

        closeSubmenu.setOnClickListener(v -> {
            bluetoothSubmenu.setVisibility(View.GONE);
            stopBluetoothDiscovery();
        });
    }


    /**
     * STORAGE PERMISSIONS
     */
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

    /**
     * BLUETOOTH PERMISSIONS
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show();
    private void checkBluetoothPermissionsAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_BT);
                return;
            }
        }
        startBluetoothDiscovery();
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    private void startBluetoothDiscovery() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        devicesList.clear();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesList);
        bluetoothDeviceList.setAdapter(devicesAdapter);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void stopBluetoothDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBluetoothDiscovery();
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(this, "Bluetooth permissions required", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * STORAGE FILE SEARCH ACCESS
     */
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
}
