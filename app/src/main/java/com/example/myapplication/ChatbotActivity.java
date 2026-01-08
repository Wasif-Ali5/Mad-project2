package com.example.myapplication;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText etMessage;
    private NestedScrollView chatScrollView;

    // Use your actual API Key here
    private final String API_KEY = "AIzaSyBB6H9D55JMWRCYCstrXAbkKe5XvaVghts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Link UI Elements
        chatContainer = findViewById(R.id.chatContainer);
        etMessage = findViewById(R.id.etMessage);
        chatScrollView = findViewById(R.id.chatScrollView);
        FloatingActionButton btnSend = findViewById(R.id.btnSend);

        // Show fancy centered welcome message
        showWelcomeMessage();

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                addMessage(msg, true); // Add User message
                etMessage.setText(""); // Clear input
                sendToGemini(msg);     // Send to AI
            }
        });
    }

    private void showWelcomeMessage() {
        TextView tvWelcome = new TextView(this);
        tvWelcome.setText("✨ AI Assistant Ready\nHow can I help you today?");
        tvWelcome.setTextSize(14);
        tvWelcome.setTextColor(0xFFB0B0B0); // Subtle Gray
        tvWelcome.setGravity(Gravity.CENTER);
        tvWelcome.setPadding(0, 50, 0, 50);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvWelcome.setLayoutParams(params);
        chatContainer.addView(tvWelcome);
    }

    private void addMessage(String text, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(isUser ? 0xFFFFFFFF : 0xFFE0E0E0);
        tv.setPadding(35, 25, 35, 25);

        // Set bubble drawables
        tv.setBackgroundResource(isUser ? R.drawable.user_bubble : R.drawable.ai_bubble);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 12;
        params.bottomMargin = 12;

        if (isUser) {
            params.gravity = Gravity.END;
            params.setMarginStart(120); // Push away from left edge
        } else {
            params.gravity = Gravity.START;
            params.setMarginEnd(120); // Push away from right edge
        }
        tv.setLayoutParams(params);

        chatContainer.addView(tv);

        // Auto-scroll to bottom
        chatScrollView.post(() -> chatScrollView.fullScroll(NestedScrollView.FOCUS_DOWN));
    }

    private void sendToGemini(String userMessage) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Build the Request JSON
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject part = new JSONObject();

            part.put("text", userMessage);
            partsArray.put(part);
            content.put("parts", partsArray);
            contents.put(content);
            body.put("contents", contents);

            // Using the latest Gemini 2.0 Flash endpoint
            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + API_KEY)
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> addMessage("Network error: Check your connection.", false));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        if (response.code() == 429) {
                            runOnUiThread(() -> addMessage("Rate limit exceeded. Please wait a moment before sending again.", false));
                            return;
                        }
                        String errorMsg = "API Error: " + response.code();
                        runOnUiThread(() -> addMessage(errorMsg, false));
                        return;
                    }

                    try {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);

                        // Extract text from the nested JSON response
                        if (json.has("candidates")) {
                            JSONArray candidates = json.getJSONArray("candidates");
                            if (candidates.length() > 0) {
                                JSONObject firstCandidate = candidates.getJSONObject(0);
                                JSONObject contentObj = firstCandidate.getJSONObject("content");
                                JSONArray parts = contentObj.getJSONArray("parts");
                                if (parts.length() > 0) {
                                    String reply = parts.getJSONObject(0).getString("text");
                                    runOnUiThread(() -> addMessage(reply, false));
                                    return;
                                }
                            }
                        }
                        runOnUiThread(() -> addMessage("Received an empty response from AI.", false));

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> addMessage("Parsing Error: Unable to read AI response.", false));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> addMessage("Critical Error: Request failed.", false));
        }
    }
}