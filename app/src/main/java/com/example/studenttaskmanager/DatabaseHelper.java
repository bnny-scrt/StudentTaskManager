package com.example.studenttaskmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database manager. Stores user accounts and all student tasks,
 * so the data persists even after the application is closed.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StudentTaskManager.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_TASKS = "tasks";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String users = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL)";
        db.execSQL(users);

        String tasks = "CREATE TABLE " + TABLE_TASKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "due_date TEXT, " +
                "completed INTEGER DEFAULT 0)";
        db.execSQL(tasks);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // ==================== USER ACCOUNTS ====================

    /** Creates a new account. Returns false when the username already exists. */
    public boolean addUser(String username, String password) {
        if (usernameExists(username)) return false;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    private boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE username = ?",
                new String[]{username});

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /** Verifies a username/password pair. Returns true on a valid login. */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE username = ? AND password = ?",
                new String[]{username, password});

        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }

    // ==================== TASKS ====================

    /** Adds a task. Returns true on success. */
    public boolean addTask(String title, String description, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("due_date", dueDate);
        values.put("completed", 0);

        long result = db.insert(TABLE_TASKS, null, values);
        return result != -1;
    }

    /**
     * Returns all tasks, pending first, then by due date, newest id first.
     * Completed tasks move to the bottom of the list.
     */
    public List<Task> getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT id, title, description, due_date, completed FROM " + TABLE_TASKS +
                        " ORDER BY completed ASC, due_date ASC, id DESC", null);

        while (cursor.moveToNext()) {
            tasks.add(new Task(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4)));
        }

        cursor.close();
        return tasks;
    }

    /** Returns a single task by id, or null when not found. */
    public Task getTask(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, title, description, due_date, completed FROM " + TABLE_TASKS +
                        " WHERE id = ?",
                new String[]{String.valueOf(id)});

        Task task = null;
        if (cursor.moveToFirst()) {
            task = new Task(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4));
        }

        cursor.close();
        return task;
    }

    /** Updates title, description and due date. Returns true when a row changed. */
    public boolean updateTask(int id, String title, String description, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("due_date", dueDate);

        int result = db.update(TABLE_TASKS, values, "id = ?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }

    /** Deletes a task. Returns true when a row was removed. */
    public boolean deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(TABLE_TASKS, "id = ?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }

    /** Sets the status of a task (0 = pending, 1 = completed). */
    public boolean updateStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("completed", status);

        int result = db.update(TABLE_TASKS, values, "id = ?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
}
