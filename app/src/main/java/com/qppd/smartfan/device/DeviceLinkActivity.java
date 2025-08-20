package com.qppd.smartfan.device;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qppd.smartfan.R;

public class DeviceLinkActivity extends AppCompatActivity {
    private EditText deviceIdEditText;
    private Button linkButton;
    private ProgressBar progressBar;
    private DatabaseReference dbRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_link);

        deviceIdEditText = findViewById(R.id.editTextDeviceId);
        linkButton = findViewById(R.id.buttonLinkDevice);
        progressBar = findViewById(R.id.progressBar);
        dbRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        linkButton.setOnClickListener(v -> linkDevice());
    }

    private void linkDevice() {
        String deviceId = deviceIdEditText.getText().toString().trim();
        if (TextUtils.isEmpty(deviceId)) {
            deviceIdEditText.setError("Device ID is required");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        // Check if device exists in DB
        dbRef.child("devices").child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Link device to user
                    dbRef.child("users").child(user.getUid()).child("devices").child(deviceId).setValue(true);
                    dbRef.child("devices").child(deviceId).child("owner").setValue(user.getUid());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DeviceLinkActivity.this, "Device linked!", Toast.LENGTH_SHORT).show();
                    // TODO: Go to dashboard
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DeviceLinkActivity.this, "Device ID not found.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DeviceLinkActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
