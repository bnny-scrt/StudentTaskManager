package com.example.studenttaskmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Data model for a single academic task.
 */
public class Task {

    private final int id;
    private final String title;
    private final String description;
    private final String dueDate; // stored as ISO yyyy-MM-dd
    private final int completed;  // 0 = pending, 1 = completed

    public Task(int id, String title, String description, String dueDate, int completed) {
        this.id = id;
        this.title = title;
        this.description = description == null ? "" : description;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public int getId() { return id; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getDueDate() { return dueDate; }

    public int getCompleted() { return completed; }

    /** Human-readable date, e.g. "Nov 15, 2026". Falls back to the raw value. */
    public String getFormattedDueDate() {
        if (dueDate == null) return "";
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dueDate);
            return new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date);
        } catch (ParseException e) {
            return dueDate;
        }
    }

    /** True when the task is still pending and its due date is before today. */
    public boolean isOverdue() {
        if (completed == 1) return false;
        try {
            Date due = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dueDate);
            Calendar startOfToday = Calendar.getInstance();
            startOfToday.set(Calendar.HOUR_OF_DAY, 0);
            startOfToday.set(Calendar.MINUTE, 0);
            startOfToday.set(Calendar.SECOND, 0);
            startOfToday.set(Calendar.MILLISECOND, 0);
            return due != null && due.before(startOfToday.getTime());
        } catch (ParseException | NullPointerException e) {
            return false;
        }
    }
}
