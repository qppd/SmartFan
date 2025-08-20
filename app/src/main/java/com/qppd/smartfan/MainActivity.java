package com.qppd.smartfan;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import com.github.anastr.speedviewlib.SpeedView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private String uid;
    private String linkedDeviceId;

    private SpeedView speedViewTemperature;
    private TextView textViewFanStatus, textViewFanSpeedLabel;
    private Switch switchAutoMode;
    private SeekBar seekBarFanSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // Not logged in, redirect to login
            startActivity(new Intent(MainActivity.this, com.qppd.smartfan.auth.LoginActivity.class));
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance().getReference();
        uid = mAuth.getCurrentUser().getUid();

        // Check if user has linked device
        dbRef.child("users").child(uid).child("devices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    // No device linked, redirect
                    startActivity(new Intent(MainActivity.this, com.qppd.smartfan.device.DeviceLinkActivity.class));
                    finish();
                } else {
                    // Get first linked device
                    for (DataSnapshot deviceSnap : snapshot.getChildren()) {
                        linkedDeviceId = deviceSnap.getKey();
                        break;
                    }
                    setupDashboard();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Optionally handle error
            }
        });
    }

    private void setupDashboard() {
    speedViewTemperature = findViewById(R.id.speedViewTemperature);
        textViewFanStatus = findViewById(R.id.textViewFanStatus);
        textViewFanSpeedLabel = findViewById(R.id.textViewFanSpeedLabel);
        switchAutoMode = findViewById(R.id.switchAutoMode);
        seekBarFanSpeed = findViewById(R.id.seekBarFanSpeed);

        // Listen for device data changes
        dbRef.child("devices").child(linkedDeviceId).child("current").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double temp = snapshot.child("temperature").getValue(Double.class);
                    Long fanSpeed = snapshot.child("fanSpeed").getValue(Long.class);
                    String mode = snapshot.child("mode").getValue(String.class);
                    if (temp != null) speedViewTemperature.speedTo(temp.floatValue());
                    if (fanSpeed != null) {
                        textViewFanStatus.setText("Fan: " + (fanSpeed == 0 ? "Off" : "Speed " + fanSpeed));
                        seekBarFanSpeed.setProgress(fanSpeed.intValue());
                        textViewFanSpeedLabel.setText("Fan Speed: " + fanSpeed);
                    }
                    if (mode != null) switchAutoMode.setChecked("auto".equals(mode));
                    seekBarFanSpeed.setEnabled(!switchAutoMode.isChecked());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        switchAutoMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbRef.child("devices").child(linkedDeviceId).child("current").child("mode").setValue(isChecked ? "auto" : "manual");
            seekBarFanSpeed.setEnabled(!isChecked);
        });

        seekBarFanSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewFanSpeedLabel.setText("Fan Speed: " + progress);
                if (fromUser && !switchAutoMode.isChecked()) {
                    dbRef.child("devices").child(linkedDeviceId).child("current").child("fanSpeed").setValue(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, Menu.NONE, "Logout");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, com.qppd.smartfan.auth.LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}