package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private EditText inputPrompt;
    private LinearLayout chatContainer;
    private ScrollView chatScrollView;

    private DatabaseReference chatRef;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Bind UI elements
        inputPrompt = findViewById(R.id.inputPrompt);
        ImageButton btnSend = findViewById(R.id.btnSend);
        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);

        // Get current user email
        currentUserEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

        // Firebase reference to "groupChat" node
        chatRef = FirebaseDatabase.getInstance().getReference("groupChat");

        // Listen for changes in Firebase in real-time
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Clear container first
                chatContainer.removeAllViews();

                // Loop through all messages
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String message = snap.child("message").getValue(String.class);
                    String email = snap.child("email").getValue(String.class);

                    addMessageToContainer(message, email);
                }

                // Scroll to bottom whenever new message arrives
                chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        ChatActivity.this,
                        "Chat error: " + error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Send button click listener
        btnSend.setOnClickListener(v -> {
            String msg = inputPrompt.getText().toString().trim();
            if(msg.isEmpty()){
                Toast.makeText(ChatActivity.this ,
                        "Plz type any message before sending it" , Toast.LENGTH_SHORT).show();
            }
            if (!msg.isEmpty()) {
                // Push message to Firebase
                String key = chatRef.push().getKey();
                assert key != null;
                chatRef.child(key).child("message").setValue(msg);
                chatRef.child(key).child("email").setValue(currentUserEmail);

                // Clear EditText
                inputPrompt.setText("");
            }
        });
    }

    // Add a single message to the chat container
    private void addMessageToContainer(String message, String email) {
        if (message == null || email == null) return;

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(16f);
        textView.setPadding(20, 10, 20, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int sideMargin = 20;
        int topBottomMargin = 12;

        if (email.equals(currentUserEmail)) {
            params.gravity = Gravity.END;
            textView.setBackgroundResource(R.drawable.bg_user_message);
            textView.setTextColor(Color.WHITE);
            params.setMargins(sideMargin, topBottomMargin, 10, topBottomMargin);
        } else {
            params.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.bg_ai_message);
            textView.setTextColor(Color.BLACK);
            params.setMargins(10, topBottomMargin, sideMargin, topBottomMargin);
        }

        textView.setLayoutParams(params);
        chatContainer.addView(textView);
    }

}