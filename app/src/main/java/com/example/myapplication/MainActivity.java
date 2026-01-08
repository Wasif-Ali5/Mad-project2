package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Logout is a MaterialButton in your new XML, so this is fine
        com.google.android.material.button.MaterialButton logout = findViewById(R.id.btnLogout);

        // Use LinearLayout because that is what the ID is attached to in your XML
        LinearLayout btnQr = findViewById(R.id.btnQR);
        LinearLayout btnChatbot = findViewById(R.id.btnChatbot);
        RelativeLayout btnUserChat = findViewById(R.id.btnChat); // This one is a RelativeLayout

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnQr.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, QrScannerActivity.class));
        });

        btnChatbot.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatbotActivity.class));
        });

        btnUserChat.setOnClickListener(v -> {
            // Add your User Chat activity here
            // startActivity(new Intent(MainActivity.this, UserChatActivity.class));
        });
    }

}
