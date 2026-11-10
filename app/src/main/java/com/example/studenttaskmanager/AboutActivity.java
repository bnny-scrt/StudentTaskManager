package com.example.studenttaskmanager;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Edit these values in res/values/strings.xml before submitting:
        // dev_name, dev_course, dev_school, dev_email
        TextView txtDeveloper = findViewById(R.id.txtDeveloper);
        txtDeveloper.setText(getString(R.string.dev_name));
        TextView txtOsVersion = findViewById(R.id.txtOsVersion);
        txtOsVersion.setText("Android " + Build.VERSION.RELEASE);
    }
}
