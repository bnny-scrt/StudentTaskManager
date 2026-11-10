package com.example.studenttaskmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard + Task List screen.
 * Shows task summary stats, a filterable RecyclerView of all saved tasks,
 * and entry points to add/edit/delete tasks and the About screen.
 */
public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private DatabaseHelper databaseHelper;
    private SharedPreferences prefs;
    private TaskAdapter adapter;

    private final List<Task> allTasks = new ArrayList<>();
    private String filter = "all"; // all | pending | completed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("session", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        // Session guard: send the user back to the login screen.
        if (!prefs.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        TextView txtGreeting = findViewById(R.id.txtGreeting);
        TextView txtToday = findViewById(R.id.txtToday);
        RecyclerView recyclerTasks = findViewById(R.id.recyclerTasks);
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        ImageButton fabAdd = findViewById(R.id.fabAdd);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnAbout = findViewById(R.id.btnAbout);
        View btnEmptyAdd = findViewById(R.id.btnEmptyAdd);

        // Greeting + today's date
        String username = prefs.getString("username", "Student");
        txtGreeting.setText(getString(R.string.greeting, username));
        txtToday.setText(new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
                .format(new Date()));

        // RecyclerView
        adapter = new TaskAdapter(this);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setAdapter(adapter);

        // Filter chips
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPending) {
                filter = "pending";
            } else if (checkedId == R.id.chipCompleted) {
                filter = "completed";
            } else {
                filter = "all";
            }
            displayTasks();
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditTaskActivity.class)));

        btnAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        btnLogout.setOnClickListener(v -> confirmLogout());

        // Empty state CTA opens the same Add Task screen as the floating + orb.
        btnEmptyAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditTaskActivity.class)));

        displayTasks();
    }

    // ==================== DISPLAY & SUMMARY ====================

    private void displayTasks() {
        allTasks.clear();
        allTasks.addAll(databaseHelper.getAllTasks());

        // Dashboard summary counts
        int pending = 0;
        int completed = 0;
        for (Task task : allTasks) {
            if (task.getCompleted() == 1) completed++;
            else pending++;
        }

        ((TextView) findViewById(R.id.statTotal)).setText(String.valueOf(allTasks.size()));
        ((TextView) findViewById(R.id.statPending)).setText(String.valueOf(pending));
        ((TextView) findViewById(R.id.statCompleted)).setText(String.valueOf(completed));

        // Progress summary bar + counters on the filter chips
        View progressRow = findViewById(R.id.layoutProgress);
        if (allTasks.isEmpty()) {
            progressRow.setVisibility(View.GONE);
        } else {
            progressRow.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtProgress))
                    .setText(getString(R.string.progress_fmt, completed, allTasks.size()));
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setMax(allTasks.size());
            progressBar.setProgress(completed);
        }
        ((TextView) findViewById(R.id.chipAll))
                .setText(getString(R.string.filter_all) + " (" + allTasks.size() + ")");
        ((TextView) findViewById(R.id.chipPending))
                .setText(getString(R.string.filter_pending) + " (" + pending + ")");
        ((TextView) findViewById(R.id.chipCompleted))
                .setText(getString(R.string.filter_completed) + " (" + completed + ")");

        // Apply the active filter
        List<Task> filtered = new ArrayList<>();
        for (Task task : allTasks) {
            if (filter.equals("pending") && task.getCompleted() != 0) continue;
            if (filter.equals("completed") && task.getCompleted() != 1) continue;
            filtered.add(task);
        }
        adapter.setTasks(filtered);

        // Show a friendly empty state when nothing matches
        boolean empty = filtered.isEmpty();
        findViewById(R.id.emptyState).setVisibility(empty ? View.VISIBLE : View.GONE);
        findViewById(R.id.recyclerTasks).setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayTasks();
    }

    // ==================== TASK ACTIONS ====================

    @Override
    public void onTaskToggleRequested(Task task) {
        int oldStatus = task.getCompleted();
        int newStatus = oldStatus == 1 ? 0 : 1;
        databaseHelper.updateStatus(task.getId(), newStatus);
        displayTasks();

        Snackbar.make(findViewById(R.id.fabAdd),
                        newStatus == 1 ? R.string.toast_completed : R.string.toast_pending,
                        Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, v -> {
                    databaseHelper.updateStatus(task.getId(), oldStatus);
                    displayTasks();
                })
                .show();
    }

    @Override
    public void onTaskMenuRequested(Task task) {
        String[] options = {getString(R.string.edit_task), getString(R.string.delete)};

        new MaterialAlertDialogBuilder(this)
                .setTitle(task.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, AddEditTaskActivity.class);
                        intent.putExtra("task_id", task.getId());
                        startActivity(intent);
                    } else {
                        confirmDelete(task);
                    }
                })
                .show();
    }

    private void confirmDelete(Task task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, task.getTitle()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    databaseHelper.deleteTask(task.getId());
                    displayTasks();
                    Snackbar.make(findViewById(R.id.fabAdd),
                            R.string.toast_deleted, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    prefs.edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
