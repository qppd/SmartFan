package com.qppd.smartfan.device;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qppd.smartfan.MainActivity;
import com.qppd.smartfan.R;

public class DeviceLinkActivity extends AppCompatActivity {
    private TextInputEditText deviceIdEditText;
    private TextInputLayout textInputLayoutDeviceId;
    private MaterialButton linkButton, createTestDeviceButton;
    private CircularProgressIndicator progressBar;
    private LinearLayout layoutProgress;
    private TextView textViewProgress;
    private MaterialCardView cardDeviceLink, cardTestDevice;
    private ImageView imageViewLogo;
    
    private DatabaseReference dbRef;
    private FirebaseUser user;
    private Handler animationHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_link);

        initializeViews();
        initializeFirebase();
        setupClickListeners();
        startEntryAnimations();
    }

    private void initializeViews() {
        deviceIdEditText = findViewById(R.id.editTextDeviceId);
        textInputLayoutDeviceId = findViewById(R.id.textInputLayoutDeviceId);
        linkButton = findViewById(R.id.buttonLinkDevice);
        createTestDeviceButton = findViewById(R.id.buttonCreateTestDevice);
        progressBar = findViewById(R.id.progressBar);
        layoutProgress = findViewById(R.id.layoutProgress);
        textViewProgress = findViewById(R.id.textViewProgress);
        cardDeviceLink = findViewById(R.id.cardDeviceLink);
        cardTestDevice = findViewById(R.id.cardTestDevice);
        imageViewLogo = findViewById(R.id.imageViewLogo);
    }

    private void initializeFirebase() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setupClickListeners() {
        linkButton.setOnClickListener(v -> {
            animateButtonPress(linkButton);
            linkDevice();
        });
        
        createTestDeviceButton.setOnClickListener(v -> {
            animateButtonPress(createTestDeviceButton);
            createTestDevice();
        });

        findViewById(R.id.textViewHelp).setOnClickListener(v -> showHelpDialog());
    }

    private void startEntryAnimations() {
        // Fade in logo with rotation
        imageViewLogo.setAlpha(0f);
        imageViewLogo.setScaleX(0.5f);
        imageViewLogo.setScaleY(0.5f);
        imageViewLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Slide in cards from bottom with stagger
        animateCardEntry(cardDeviceLink, 200);
        animateCardEntry(cardTestDevice, 400);
    }

    private void animateCardEntry(MaterialCardView card, long delay) {
        card.setTranslationY(300f);
        card.setAlpha(0f);
        card.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateButtonPress(MaterialButton button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    button.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void linkDevice() {
        String deviceId = deviceIdEditText.getText().toString().trim().toUpperCase();
        if (TextUtils.isEmpty(deviceId)) {
            textInputLayoutDeviceId.setError("Device ID is required");
            shakeView(textInputLayoutDeviceId);
            return;
        }
        
        if (deviceId.length() < 8) {
            textInputLayoutDeviceId.setError("Device ID must be at least 8 characters");
            shakeView(textInputLayoutDeviceId);
            return;
        }
        
        textInputLayoutDeviceId.setError(null);
        showProgress("Searching for device...");
        setButtonsEnabled(false);
        
        // Check if device exists in DB
        dbRef.child("devices").child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    textViewProgress.setText("Linking device...");
                    
                    // Link device to user
                    dbRef.child("users").child(user.getUid()).child("devices").child(deviceId).setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            dbRef.child("devices").child(deviceId).child("owner").setValue(user.getUid())
                                .addOnSuccessListener(aVoid2 -> {
                                    textViewProgress.setText("Success!");
                                    showSuccessAnimation(() -> {
                                        hideProgress();
                                        showSuccessSnackbar("Device linked successfully!");
                                        
                                        // Navigate to MainActivity after delay
                                        animationHandler.postDelayed(() -> {
                                            Intent intent = new Intent(DeviceLinkActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        }, 1500);
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    hideProgress();
                                    setButtonsEnabled(true);
                                    showErrorSnackbar("Failed to link device: " + e.getMessage());
                                });
                        })
                        .addOnFailureListener(e -> {
                            hideProgress();
                            setButtonsEnabled(true);
                            showErrorSnackbar("Failed to link device: " + e.getMessage());
                        });
                } else {
                    hideProgress();
                    setButtonsEnabled(true);
                    textInputLayoutDeviceId.setError("Device not found");
                    shakeView(textInputLayoutDeviceId);
                    showErrorSnackbar("Device ID not found. Please check the ID or create a test device.");
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                hideProgress();
                setButtonsEnabled(true);
                showErrorSnackbar("Connection error: " + error.getMessage());
            }
        });
    }
    
    private void createTestDevice() {
        String testDeviceId = "TEST_DEVICE_001";
        showProgress("Creating test device...");
        setButtonsEnabled(false);
        
        // Create a test device with mock data
        DatabaseReference testDeviceRef = dbRef.child("devices").child(testDeviceId);
        
        // Set basic device info
        testDeviceRef.child("name").setValue("Smart Fan Test Device");
        testDeviceRef.child("model").setValue("SF-2024-PRO");
        testDeviceRef.child("firmware").setValue("1.2.3");
        testDeviceRef.child("serialNumber").setValue("SF240001");
        
        // Set current status
        DatabaseReference currentRef = testDeviceRef.child("current");
        currentRef.child("temperature").setValue(25.5);
        currentRef.child("fanSpeed").setValue(50);
        currentRef.child("mode").setValue("auto");
        currentRef.child("isOnline").setValue(true);
        currentRef.child("lastSeen").setValue(System.currentTimeMillis());
        currentRef.child("powerConsumption").setValue(15.2);
        currentRef.child("humidity").setValue(45.0);
        
        // Set default settings
        DatabaseReference settingsRef = testDeviceRef.child("settings");
        settingsRef.child("targetTemperature").setValue(24.0);
        settingsRef.child("autoMode").setValue(true);
        settingsRef.child("maxSpeed").setValue(100);
        settingsRef.child("minSpeed").setValue(10);
        settingsRef.child("schedule").child("enabled").setValue(false);
        
        textViewProgress.setText("Test device created!");
        
        animationHandler.postDelayed(() -> {
            hideProgress();
            setButtonsEnabled(true);
            deviceIdEditText.setText(testDeviceId);
            
            // Highlight the device ID field
            textInputLayoutDeviceId.setBoxStrokeColor(getColor(R.color.accent_green));
            
            showSuccessSnackbar("Test device created! Device ID: " + testDeviceId);
            
            // Auto-populate and animate
            animateTextEntry(deviceIdEditText, testDeviceId);
            
        }, 1500);
    }

    private void showProgress(String message) {
        textViewProgress.setText(message);
        layoutProgress.setVisibility(View.VISIBLE);
        layoutProgress.setAlpha(0f);
        layoutProgress.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void hideProgress() {
        layoutProgress.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> layoutProgress.setVisibility(View.GONE))
                .start();
    }

    private void setButtonsEnabled(boolean enabled) {
        linkButton.setEnabled(enabled);
        createTestDeviceButton.setEnabled(enabled);
        linkButton.setAlpha(enabled ? 1f : 0.6f);
        createTestDeviceButton.setAlpha(enabled ? 1f : 0.6f);
    }

    private void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(500);
        shake.start();
    }

    private void animateTextEntry(TextInputEditText editText, String text) {
        editText.setText("");
        ValueAnimator animator = ValueAnimator.ofInt(0, text.length());
        animator.setDuration(text.length() * 50);
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            editText.setText(text.substring(0, progress));
        });
        animator.start();
    }

    private void showSuccessAnimation(Runnable onComplete) {
        // Success checkmark animation could be added here
        if (onComplete != null) {
            animationHandler.postDelayed(onComplete, 800);
        }
    }

    private void showSuccessSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getColor(R.color.accent_green));
        snackbar.setTextColor(getColor(R.color.neutral_white));
        snackbar.show();
    }

    private void showErrorSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getColor(R.color.accent_red));
        snackbar.setTextColor(getColor(R.color.neutral_white));
        snackbar.show();
    }

    private void showHelpDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Device Linking Help")
                .setMessage("To link your Smart Fan:\n\n" +
                           "1. Find the Device ID on your fan's display or manual\n" +
                           "2. Enter the ID in the format: SF-XXXXXXXXX\n" +
                           "3. Tap 'Link Device'\n\n" +
                           "For testing purposes, you can create a virtual device using the 'Create Test Device' button.")
                .setPositiveButton("Got it", null)
                .setIcon(R.drawable.ic_info)
                .show();
    }
}
