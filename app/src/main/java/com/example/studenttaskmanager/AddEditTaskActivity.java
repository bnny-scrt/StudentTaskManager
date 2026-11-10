package com.example.studenttaskmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Add Task and Edit Task share this activity.
 * Pass "task_id" as an intent extra to enter edit mode.
 */
public class AddEditTaskActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextInputLayout tilTitle, tilDueDate;
    private TextInputEditText edtTitle, edtDescription, edtDueDate;
    private Button btnSave, btnDelete;

    private int taskId = -1;
    private String dueDate = ""; // ISO format: yyyy-MM-dd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        databaseHelper = new DatabaseHelper(this);

        TextView txtScreenTitle = findViewById(R.id.txtScreenTitle);
        ImageButton btnBack = findViewById(R.id.btnBack);
        tilTitle = findViewById(R.id.tilTitle);
        tilDueDate = findViewById(R.id.tilDueDate);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtDueDate = findViewById(R.id.edtDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        btnBack.setOnClickListener(v -> finish());
        edtDueDate.setOnClickListener(v -> showDatePicker());
        tilDueDate.setEndIconOnClickListener(v -> showDatePicker());
        findViewById(R.id.tilDueDate).setOnClickListener(v -> showDatePicker());

        taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId != -1) {
            txtScreenTitle.setText(R.string.edit_task_title);
            btnDelete.setVisibility(View.VISIBLE);
            loadTask(taskId);
        }

        btnSave.setOnClickListener(v -> saveTask());
        btnDelete.setOnClickListener(v -> confirmDelete());

        // Inline validation: check title as soon as the user leaves the field.
        edtTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (edtTitle.getText().toString().trim().isEmpty()) {
                    tilTitle.setError(getString(R.string.error_title_required));
                } else {
                    tilTitle.setError(null);
                }
            }
        });
    }

    private void loadTask(int id) {
        Task task = databaseHelper.getTask(id);
        if (task == null) {
            finish();
            return;
        }
        edtTitle.setText(task.getTitle());
        edtDescription.setText(task.getDescription());
        dueDate = task.getDueDate();
        edtDueDate.setText(task.getFormattedDueDate());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_due_date)
                .setTheme(R.style.GlowCalendar)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            // MaterialDatePicker returns UTC midnight — format in UTC so
            // the chosen day never shifts by one when converted locally.
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));
            dueDate = iso.format(new Date(selection));

            SimpleDateFormat display = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            display.setTimeZone(TimeZone.getTimeZone("UTC"));
            edtDueDate.setText(display.format(new Date(selection)));
            tilDueDate.setError(null);
        });

        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void saveTask() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        boolean valid = true;
        if (title.isEmpty()) {
            tilTitle.setError(getString(R.string.error_title_required));
            valid = false;
        } else {
            tilTitle.setError(null);
        }
        if (dueDate.isEmpty()) {
            tilDueDate.setError(getString(R.string.error_date_required));
            valid = false;
        } else {
            tilDueDate.setError(null);
        }
        if (!valid) return;

        boolean result;
        if (taskId == -1) {
            result = databaseHelper.addTask(title, description, dueDate);
            Toast.makeText(this, R.string.toast_added, Toast.LENGTH_SHORT).show();
        } else {
            result = databaseHelper.updateTask(taskId, title, description, dueDate);
            Toast.makeText(this, R.string.toast_updated, Toast.LENGTH_SHORT).show();
        }

        setResult(result ? RESULT_OK : RESULT_CANCELED);
        finish();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message,
                        edtTitle.getText().toString().trim()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    databaseHelper.deleteTask(taskId);
                    Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
