# Student Task Manager
## Android Application Project Documentation

**IT Project — Mobile Application Development**

**Name:** Your Name
**Course / Section:** BSIT — Your Section
**University:** Your University
**Date Submitted:** November 2026

---

## 1. Introduction

University students handle a constant stream of academic responsibilities: assignments, projects, quizzes, group work, and exam reviews. Most of these responsibilities are personally tracked using paper planners, sticky notes, or mental notes, all of which are easy to lose and even easier to forget. When deadlines pile up, students tend to miss submissions not because they are incapable of doing the work, but because there is no reliable, always-available system that reminds them of what needs to be done.

**Student Task Manager** is an Android application developed to address this problem. It allows students to record, organize, and track their academic tasks using their smartphone. The application works completely offline: every task is saved in a local database on the device, so the student's entire task history remains available even if the application is closed, the phone is restarted, or the device loses internet connection.

This document presents the project's objectives, the technologies used, the system design, the database structure, the main features, the testing performed, and the developer's reflections on the development process.

---

## 2. Objectives

The general objective of the project is to develop a functional mobile application that helps students manage their academic activities, assignments, projects, deadlines, and schedules using an Android smartphone.

The specific objectives are:

1. To design a clean and user-friendly interface consisting of a Login Screen, a Dashboard, and an About Screen, following Material Design guidelines.
2. To implement user login so that a student can create an account and secure their personal task list.
3. To implement full task management (Create, Read, Update, and Delete) for academic tasks.
4. To allow each task to carry a due date and a status that is either **Pending** or **Completed**.
5. To display all saved tasks efficiently using RecyclerView and to show task summaries (total, pending, completed) on a dashboard.
6. To store all data persistently in a local SQLite database so no task is ever lost after closing the application.

---

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Android Studio | Official development environment (IDE) |
| Java | Core programming language for all application logic |
| XML | User interface design (all screens, drawables, themes) |
| SQLite | Local relational database for accounts and task records |
| RecyclerView | Efficient scrolling list for displaying task records |
| Material Design (Material 3) | Buttons, input fields, chips, cards, FAB, dialogs |
| SharedPreferences | Storage of the login session between app sessions |

The application has `minSdk 24` (Android 7.0), which means it runs on devices from Android 7.0 up to the latest Android versions. This wide range was chosen deliberately so that students using older or budget phones can still install and use the app.

---

## 4. System Design

### 4.1 Application Flow

The application follows a simple linear flow:

1. **LoginActivity** — the app's entry point. A first-time user taps *Create Account* to register a username and password; these are stored in the database. On logging in, the session is saved so the user does not need to log in again every time the app opens.
2. **MainActivity (Dashboard + Task List)** — the main screen. It greets the user, shows today's date, and displays three summary cards: **Total**, **Pending**, and **Completed** tasks. Below the summary is a filter row (All / Pending / Completed) and the full task list.
3. **AddEditTaskActivity** — opened by tapping the floating "+" button (Add mode) or by long-pressing a task and choosing *Edit Task* (Edit mode). It collects the task title, an optional description, and a due date chosen from a calendar picker.
4. **AboutActivity** — reachable from the dashboard header; it shows the application description, its feature list, and the developer information.

### 4.2 Database Design

The database, `StudentTaskManager.db`, is managed by the `DatabaseHelper` class which extends `SQLiteOpenHelper`. It contains two tables:

**Table: users**

| Column | Type | Description |
|---|---|---|
| id | INTEGER (PK, auto-increment) | Unique account identifier |
| username | TEXT, UNIQUE, NOT NULL | Student's login name |
| password | TEXT, NOT NULL | Student's password |

**Table: tasks**

| Column | Type | Description |
|---|---|---|
| id | INTEGER (PK, auto-increment) | Unique task identifier |
| title | TEXT, NOT NULL | Task name (e.g., "Mock Exam Review") |
| description | TEXT | Optional details about the task |
| due_date | TEXT | Deadline stored in ISO format (yyyy-MM-dd) for correct sorting |
| completed | INTEGER, default 0 | Status flag: 0 = Pending, 1 = Completed |

The database exposes clean methods used by the activities: `addUser()`, `checkUser()`, `addTask()`, `getAllTasks()`, `getTask()`, `updateTask()`, `deleteTask()`, and `updateStatus()`. Task records are returned sorted so that pending tasks appear first, ordered by due date, which naturally puts the most urgent work at the top.

### 4.3 Screens and User Interface

All layouts were built in XML following a **black "glow" theme**: a pure-black background (#000000) with near-black cards (#121212) and hairline borders, white primary text with light-gray secondary text (all contrast ratios stay above the 4.5:1 accessibility minimum), and **glowing white buttons** created with layered translucent stroke drawables that simulate a neon halo, all with ripple pressed feedback. The login title and tagline use additional text-shadow glows, and the Material date-picker calendar is themed with the same black-and-white palette. Every interactive control meets the 48dp minimum touch-target guideline.

The task items themselves are rendered by `TaskAdapter` in a RecyclerView. Each card shows the task title, a short description preview, the formatted due date, and a colored status chip — green for *Completed*, orange for *Pending*, and red for *Overdue* (a pending task whose deadline has already passed). Tapping a card toggles its status with an *Undo* snackbar; long-pressing opens a menu with *Edit Task* and *Delete Task*.

---

## 5. Key Features

1. **Login Screen** — account creation, login validation with clear error feedback, and a persistent session. Logging out can be done from the dashboard.
2. **Dashboard** — live summary of Total / Pending / Completed tasks plus filters so the student can focus only on pending work.
3. **Add Task** — title and due date are required; the due date is chosen through a native calendar dialog to avoid typing mistakes. Validation messages are shown directly under the missing field.
4. **Edit Task** — existing tasks are re-opened pre-filled; the user can update the title, description, or due date.
5. **Delete Task** — always confirmed with a dialog that names the exact task ("Delete *Mock Exam Review*? This cannot be undone."), preventing accidental deletion.
6. **Task Status** — one tap flips a task between Pending and Completed; completed tasks move to the bottom of the list and the button can be undone.
7. **Due Date & Overdue Detection** — the app compares stored dates with today's date and automatically flags missed deadlines in red.
8. **About Screen** — application description, feature list, and developer information.
9. **Empty State** — when no tasks match a filter, the app shows a friendly illustration-style message ("No tasks yet — tap + to add your first academic task") instead of a blank screen.

*(Insert labeled screenshots of each screen here: Figure 1 – Login Screen, Figure 2 – Dashboard, Figure 3 – Add Task with calendar picker, Figure 4 – Task List with filters, Figure 5 – Edit Task, Figure 6 – Delete Confirmation, Figure 7 – About Screen.)*

---

## 6. Testing

Testing was performed on an Android emulator using both functional testing (each feature is exercised step by step) and input validation testing (wrong or empty inputs are deliberately entered). The results are summarized below.

| # | Test Case | Expected Result | Actual Result |
|---|---|---|---|
| 1 | Launch app without a saved session | Login screen appears | ✅ Pass |
| 2 | Create account with a new username | "Account created" toast; account stored | ✅ Pass |
| 3 | Create account with an existing username | "Username already taken" error | ✅ Pass |
| 4 | Login with a wrong password | "Invalid username or password" error; stays on login | ✅ Pass |
| 5 | Login with valid credentials | Dashboard opens with greeting and date | ✅ Pass |
| 6 | Reopen the app after logging in | Goes straight to dashboard (session retained) | ✅ Pass |
| 7 | Add Task with empty fields | Inline errors under missing fields; no saving | ✅ Pass |
| 8 | Add a complete task | Task appears at top of list; counters update | ✅ Pass |
| 9 | Tap a task | Status changes Pending → Completed with UNDO option | ✅ Pass |
| 10 | Long-press a task | Menu with Edit / Delete appears | ✅ Pass |
| 11 | Edit a task's due date | Changes are saved and displayed | ✅ Pass |
| 12 | Delete a task after confirmation | Task disappears; "Task deleted" message | ✅ Pass |
| 13 | Filter by Pending / Completed | List shows only tasks of the chosen status | ✅ Pass |
| 14 | Task with a past due date | Card shows red OVERDUE badge | ✅ Pass |
| 15 | Close and reopen the app | All tasks still present (SQLite persistence) | ✅ Pass |
| 16 | Open About screen | App and developer information displayed | ✅ Pass |

All 16 test cases passed. No crashes were encountered during repetitive testing (adding, editing, toggling, and deleting five tasks in one session).

---

## 7. Challenges Encountered

1. **Screen overlap with system bars.** Early versions placed the back and about buttons underneath the phone's status bar because newer Android versions force apps to draw edge-to-edge. This was resolved by enabling `fitsSystemWindows` on every screen so the interface is always pushed safely below the system bars.
2. **Date storage and sorting.** Storing dates as readable text ("Nov 15, 2026") made sorting unreliable. The fix was to store every due date in ISO format (`yyyy-MM-dd`) — which sorts chronologically as plain text — and convert to the friendly format only when displaying.
3. **Duplicate class conflict.** The starter project initially contained both a Kotlin and a Java `MainActivity`; both had to be consolidated into a single Java implementation to allow the build to succeed.
4. **Layout resources.** Some Material attributes used in the initial prototype were removed in newer library versions (e.g., `checkedChipId`), producing resource linking errors. These were replaced with programmatic equivalents, and the lesson learned was to verify attribute compatibility against the actual dependency version in use.

---

## 8. Conclusion

The Student Task Manager application successfully meets all of its objectives. It provides a complete, offline-capable academic task management experience on a student's Android phone: secure login, a dashboard with live summaries, full CRUD operations, filters, due-date tracking with automatic overdue detection, and a polished Material Design interface built entirely with Java, XML, and SQLite.

Beyond the working application, the project provided valuable hands-on experience in the complete Android development workflow — designing layouts in XML, structuring data in SQLite, wiring adapters to RecyclerView, handling user input validation, and debugging resource and compatibility issues. The project also demonstrated disciplined development across a three-week schedule: interface prototyping first, functionality second, and testing and documentation last.

**Possible future improvements** for the next iteration include: password hashing for improved account security, multiple accounts with per-user task separation, reminder notifications before deadlines, and exporting tasks to a shareable file.

---

## 9. References

1. Android Developers. "Save data using SQLite." developer.android.com
2. Android Developers. "Create dynamic lists with RecyclerView." developer.android.com
3. Material Design 3. "Component library: Buttons, Chips, Text Fields, Cards." m3.material.io
4. Oracle. "Java SE Documentation." docs.oracle.com
5. Android Developers. "App quality guidelines and accessibility best practices." developer.android.com
