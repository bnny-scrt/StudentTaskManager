package com.example.studenttaskmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SharedPreferences prefs;
    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText edtUsername, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("session", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        // Already logged in? Go straight to the dashboard.
        if (prefs.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Errors live immediately when the user fixes the field.
        TextWatcher fieldFixer = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                tilUsername.setError(null);
                tilPassword.setError(null);
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        edtUsername.addTextChangedListener(fieldFixer);
        edtPassword.addTextChangedListener(fieldFixer);

        btnLogin.setOnClickListener(v -> login());
        btnCreateAccount.setOnClickListener(v -> createAccount());
    }

    private void login() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty()) tilUsername.setError(getString(R.string.toast_login_empty));
            if (password.isEmpty()) tilPassword.setError(getString(R.string.toast_login_empty));
            return;
        }

        if (databaseHelper.checkUser(username, password)) {
            prefs.edit().putBoolean("logged_in", true).putString("username", username).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Specific inline error on the password field, not just a toast.
            tilPassword.setError(getString(R.string.toast_login_invalid));
        }
    }

    private void createAccount() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        boolean valid = true;
        if (username.length() < 3) {
            tilUsername.setError(getString(R.string.error_username_short));
            valid = false;
        }
        if (password.length() < 4) {
            tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!valid) return;

        if (databaseHelper.addUser(username, password)) {
            Toast.makeText(this, R.string.toast_account_created, Toast.LENGTH_SHORT).show();
            edtPassword.setText("");
        } else {
            tilUsername.setError(getString(R.string.toast_account_exists));
        }
    }
}
