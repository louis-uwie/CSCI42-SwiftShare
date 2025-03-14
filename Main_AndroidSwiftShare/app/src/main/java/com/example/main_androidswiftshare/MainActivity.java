package com.example.main_androidswiftshare;

import android.Manifest;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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

}