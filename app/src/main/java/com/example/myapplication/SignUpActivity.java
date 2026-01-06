package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextInputEditText etEmail, etPassword, etConfirm;
    TextInputLayout emailL, passL, confirmL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);

        emailL = (TextInputLayout) etEmail.getParent().getParent();
        passL = (TextInputLayout) etPassword.getParent().getParent();
        confirmL = (TextInputLayout) etConfirm.getParent().getParent();

        Button btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(v -> register());
    }

    private void register() {
        String e = etEmail.getText().toString().trim();
        String p = etPassword.getText().toString().trim();
        String c = etConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(e)) {
            emailL.setError("Required");
            return;
        }
        if (p.length() < 6) {
            passL.setError("Min 6 chars");
            return;
        }
        if (!p.equals(c)) {
            confirmL.setError("Passwords mismatch");
            return;
        }

        mAuth.createUserWithEmailAndPassword(e, p)
                .addOnSuccessListener(a -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(er ->
                        Toast.makeText(this, er.getMessage(), Toast.LENGTH_LONG).show());
    }
}
