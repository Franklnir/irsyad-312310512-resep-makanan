package com.example.project111;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class device extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private LocationManager locationManager;
    private String locationInfo = "Fetching location...";
    private StringBuilder deviceDetails;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device);

        TextView deviceInfo = findViewById(R.id.deviceInfo);
        Button btnSaveDevice = findViewById(R.id.btnSaveDevice);

        deviceDetails = new StringBuilder();
        database = FirebaseDatabase.getInstance().getReference("users");

        // Ambil username dari Intent
        String username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Username tidak ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Informasi Perangkat Dasar
        deviceDetails.append("Model: ").append(Build.MODEL).append("\n")
                .append("Brand: ").append(Build.BRAND).append("\n")
                .append("Product: ").append(Build.PRODUCT).append("\n")
                .append("Android Version: ").append(Build.VERSION.RELEASE).append("\n")
                .append("SDK Level: ").append(Build.VERSION.SDK_INT).append("\n\n");

        // RAM
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        deviceDetails.append("Total RAM: ").append(memoryInfo.totalMem / (1024 * 1024)).append(" MB\n");

        // Penyimpanan
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long totalStorage = statFs.getTotalBytes() / (1024 * 1024);
        long availableStorage = statFs.getAvailableBytes() / (1024 * 1024);
        deviceDetails.append("Total Storage: ").append(totalStorage).append(" MB\n")
                .append("Available Storage: ").append(availableStorage).append(" MB\n");

        // Jaringan Seluler
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperatorName = telephonyManager.getNetworkOperatorName();
        deviceDetails.append("Mobile Network: ").append(networkOperatorName).append("\n");

        // Jaringan Wi-Fi
        String wifiSSID = "Not Connected";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            wifiSSID = getApplicationContext().getSystemService(android.net.wifi.WifiManager.class)
                    .getConnectionInfo().getSSID();
        }
        deviceDetails.append("WiFi SSID: ").append(wifiSSID).append("\n");

        // Alamat IP
        String ipAddress = getIPAddress();
        deviceDetails.append("IP Address: ").append(ipAddress).append("\n");

        // Status Baterai
        BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        deviceDetails.append("Battery Level: ").append(batteryLevel).append("%\n");

        // Lokasi
        getLocation();
        deviceDetails.append("Location: ").append(locationInfo).append("\n");

        // Suhu Perangkat
        float deviceTemp = getDeviceTemperature();
        deviceDetails.append("Device Temperature: ").append(deviceTemp).append("°C\n");

        // Sistem Chipset
        String chipset = Build.HARDWARE;
        deviceDetails.append("Chipset: ").append(chipset).append("\n");

        // Tampilkan Informasi
        deviceInfo.setText(deviceDetails.toString());

        // Tombol Save - gabungkan fungsi simpan dan pindah aktivitas
        btnSaveDevice.setOnClickListener(view -> {
            saveDeviceInfoToDatabase(username);  // Simpan ke database
            Intent mainIntent = new Intent(device.this, MainActivity.class);  // Intent ke MainActivity
            startActivity(mainIntent);  // Mulai aktivitas baru
            finish();  // Tutup aktivitas ini agar tidak kembali ke sini
        });
    }

    private void saveDeviceInfoToDatabase(String username) {
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Username is required to save device info!", Toast.LENGTH_SHORT).show();
            return;
        }

        database.child(username).child("device_info").setValue(deviceDetails.toString())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Mulai!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to Save Device Info!", Toast.LENGTH_SHORT).show());
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            locationInfo = "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude();
            Log.d("Location", locationInfo);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    private float getDeviceTemperature() {
        try {
            File thermalFile = new File("/sys/class/thermal/thermal_zone0/temp");
            if (thermalFile.exists()) {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(thermalFile));
                String tempStr = reader.readLine();
                reader.close();
                return Float.parseFloat(tempStr) / 1000;
            }
        } catch (Exception e) {
            Log.e("Temperature", "Error reading temperature", e);
        }
        return 0.0f;
    }

    private String getIPAddress() {
        try {
            for (java.util.Enumeration<java.net.NetworkInterface> en = java.net.NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                java.net.NetworkInterface intf = en.nextElement();
                for (java.util.Enumeration<java.net.InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    java.net.InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IPAddress", "Unable to get IP Address", ex);
        }
        return "Unavailable";
    }
}
