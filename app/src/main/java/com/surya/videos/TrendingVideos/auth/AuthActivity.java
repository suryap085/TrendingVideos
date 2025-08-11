package com.surya.videos.TrendingVideos.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.surya.videos.TrendingVideos.R;
import com.surya.videos.TrendingVideos.SearchVideos.SearchActivity;

import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {
    
    private EditText phoneNumberEditText, otpEditText;
    private Button sendOtpButton, verifyOtpButton;
    private ProgressBar progressBar;
    private TextView statusText;
    
    private FirebaseAuth firebaseAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        // Initialize views
        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        otpEditText = findViewById(R.id.otp_edit_text);
        sendOtpButton = findViewById(R.id.send_otp_button);
        verifyOtpButton = findViewById(R.id.verify_otp_button);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        
        // Check if user is already signed in
        if (firebaseAuth.getCurrentUser() != null) {
            startMainActivity();
            return;
        }
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        sendOtpButton.setOnClickListener(v -> sendOTP());
        verifyOtpButton.setOnClickListener(v -> verifyOTP());
    }
    
    private void sendOTP() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError("Phone number is required");
            return;
        }
        
        // Add country code if not present
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+91" + phoneNumber; // Default to India (+91)
        }
        
        showProgress(true);
        statusText.setText("Sending OTP...");
        
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        showProgress(false);
                        signInWithCredential(credential);
                    }
                    
                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        showProgress(false);
                        statusText.setText("Verification failed: " + e.getMessage());
                        Toast.makeText(AuthActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    
                    @Override
                    public void onCodeSent(String vId, PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = vId;
                        resendToken = token;
                        showProgress(false);
                        statusText.setText("OTP sent successfully!");
                        otpEditText.setVisibility(View.VISIBLE);
                        verifyOtpButton.setVisibility(View.VISIBLE);
                        sendOtpButton.setText("Resend OTP");
                        Toast.makeText(AuthActivity.this, "OTP sent successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    
    private void verifyOTP() {
        String otp = otpEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(otp)) {
            otpEditText.setError("OTP is required");
            return;
        }
        
        if (verificationId == null) {
            Toast.makeText(this, "Please send OTP first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        statusText.setText("Verifying OTP...");
        
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithCredential(credential);
    }
    
    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        statusText.setText("Authentication successful!");
                        Toast.makeText(AuthActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        statusText.setText("Authentication failed: " + task.getException().getMessage());
                        Toast.makeText(AuthActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        sendOtpButton.setEnabled(!show);
        verifyOtpButton.setEnabled(!show);
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
