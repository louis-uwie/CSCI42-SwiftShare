package com.example.main_androidswiftshare;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button btnSend, btnReceive, btnFileLocator;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int FILE_PICKER_REQUEST_CODE = 102;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_BT = 2;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private ArrayList<String> devicesList = new ArrayList<>();
    private LinearLayout bluetoothSubmenu;
    private ListView bluetoothDeviceList;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceName = "Unknown";
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName() != null ? device.getName() : "Unknown";
                    }
                    String deviceInfo = deviceName + " - " + device.getAddress();
                    if (!devicesList.contains(deviceInfo)) {
                        devicesList.add(deviceInfo);
                        devicesAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

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
            } else {
                requestStoragePermission();
            }
        });

        btnSend.setOnClickListener(v -> Toast.makeText(this, "Send feature coming soon.", Toast.LENGTH_LONG).show());

        btnReceive.setOnClickListener(v -> Toast.makeText(this, "Receive feature coming soon.", Toast.LENGTH_LONG).show());

        Button listDevicesButton = findViewById(R.id.listDevices);
        bluetoothSubmenu = findViewById(R.id.bluetoothSubmenu);
        Button closeSubmenu = findViewById(R.id.closeSubmenu);
        bluetoothDeviceList = findViewById(R.id.bluetoothDeviceList);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesList);
        bluetoothDeviceList.setAdapter(devicesAdapter);

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show();
            return;
        }

        listDevicesButton.setOnClickListener(v -> {
            if (hasBluetoothPermissions()) {
                bluetoothSubmenu.setVisibility(View.VISIBLE);
                startBluetoothDiscovery();
            } else {
                // Always prompt again on button click
                requestBluetoothPermissions();
            }
        });




        findViewById(R.id.closeSubmenu).setOnClickListener(v -> {
            bluetoothSubmenu.setVisibility(View.GONE);
            stopBluetoothDiscovery();
        });
    }


    /**
     * LOCAL FILE ACCESS PERMISSION CHECKING
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

        ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
    }

    private void checkPermissionOnStartup() {
        if (!hasStoragePermission()) {
            requestStoragePermission();
        }
    }

    /**
     * BLUETOOTH PERMISSION ACCESS CHECKING
     */

    private boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permissions auto-granted below Android 12
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_PERMISSION_BT);
        }
    }


    private void checkBluetoothPermissionsAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_BT);
        } else {
            startBluetoothDiscovery();
        }
    }

    private void startBluetoothDiscovery() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        devicesList.clear();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Request missing BLUETOOTH_SCAN permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_PERMISSION_BT);
            return;
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_BT) {
            if (hasBluetoothPermissions()) {
                Toast.makeText(this, "Bluetooth permissions granted.", Toast.LENGTH_SHORT).show();
                bluetoothSubmenu.setVisibility(View.VISIBLE);
                startBluetoothDiscovery();
            } else {
                Toast.makeText(this, "Bluetooth permissions denied. Tap again to retry.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_LONG).show();
            }
        }
    }





    private void stopBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
            try {
                unregisterReceiver(bluetoothReceiver);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    // Direct File Access using Built-in File Search
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a File"), FILE_PICKER_REQUEST_CODE);
    }

    // Directly open app settings to modify bluetooth permissions. This is normal in other apps as well so I think its appropriate.
    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    // Stopping Bluetooth.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBluetoothDiscovery();
    }
}