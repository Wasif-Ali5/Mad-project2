package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private AdView adView;
    private final Handler adRetryHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ---------- UI ----------
        MaterialButton logout = findViewById(R.id.btnLogout);
        LinearLayout btnQr = findViewById(R.id.btnQR);
        LinearLayout btnChatbot = findViewById(R.id.btnChatbot);
        RelativeLayout btnUserChat = findViewById(R.id.btnChat);

        // ---------- AdMob TEST CONFIGURATION ----------
        MobileAds.initialize(this, initializationStatus -> {});

        // Force test ads on emulator / device
        RequestConfiguration config = new RequestConfiguration.Builder()
                .setTestDeviceIds(Collections.singletonList(AdRequest.DEVICE_ID_EMULATOR))
                .build();
        MobileAds.setRequestConfiguration(config);

        adView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Show banner when loaded
                adView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // Hide if failed
                adView.setVisibility(View.GONE);

                // Retry after 5 seconds
                adRetryHandler.postDelayed(() -> {
                    if (adView != null) {
                        adView.loadAd(new AdRequest.Builder().build());
                    }
                }, 5000);
            }
        });

        // ---------- Load Ad ----------
        adView.loadAd(adRequest);

        // ---------- Button Actions ----------
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnQr.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, QrScannerActivity.class)));

        btnChatbot.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChatbotActivity.class)));

        btnUserChat.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatActivity.class));
        });
    }

    // ---------- Ad Lifecycle ----------
    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        adRetryHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
