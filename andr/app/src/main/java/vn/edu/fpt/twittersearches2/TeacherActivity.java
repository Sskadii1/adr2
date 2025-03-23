package vn.edu.fpt.twittersearches2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;
import android.app.AlertDialog;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.view.View;
import android.widget.AdapterView;


public class TeacherActivity extends Activity {
    private DatabaseHelper dbHelper;
    private ListView listView;
    private Spinner classSpinner, studentSpinner;
    private int teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        dbHelper = new DatabaseHelper(this);
        teacherId = getIntent().getIntExtra("USER_ID", -1);

        listView = findViewById(R.id.listView);
        classSpinner = findViewById(R.id.classSpinner);
        studentSpinner = findViewById(R.id.studentSpinner);

        Button takeAttendance = findViewById(R.id.takeAttendanceButton);
        Button viewAttendance = findViewById(R.id.viewAttendanceButton);

        loadClasses();
        takeAttendance.setOnClickListener(v -> takeAttendance());
        viewAttendance.setOnClickListener(v -> viewAttendance());
    }

    private void loadClasses() {
        ArrayList<String> classes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("classes", new String[]{"id", "name"}, 
            "teacher_id = ?", new String[]{String.valueOf(teacherId)}, 
            null, null, null);

        while(cursor.moveToNext()) {
            String name = cursor.getString(1);
            classes.add(name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, classes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(adapter);

        Button attendanceButton = findViewById(R.id.attendanceButton);
        attendanceButton.setOnClickListener(v -> takeAttendance());

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStudents(classes.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadStudents(String className) {
        ArrayList<String> students = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
            "SELECT u.username FROM students s " +
            "JOIN users u ON s.user_id = u.id " +
            "JOIN classes c ON s.class_id = c.id " +
            "WHERE c.name = ?", new String[]{className});

        while(cursor.moveToNext()) {
            students.add(cursor.getString(0));
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, students);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSpinner.setAdapter(adapter);
    }

    private void takeAttendance() {
        String className = classSpinner.getSelectedItem().toString();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(
            "SELECT s.id, u.username FROM students s " +
            "JOIN users u ON s.user_id = u.id " +
            "JOIN classes c ON s.class_id = c.id " +
            "WHERE c.name = ?", new String[]{className});

        ArrayList<String> students = new ArrayList<>();
        final ArrayList<Integer> studentIds = new ArrayList<>();

        while(cursor.moveToNext()) {
            studentIds.add(cursor.getInt(0));
            students.add(cursor.getString(1));
        }
        cursor.close();

        for(int i = 0; i < students.size(); i++) {
            final int studentId = studentIds.get(i);
            final String studentName = students.get(i);

            new AlertDialog.Builder(this)
                .setTitle("Attendance for " + studentName)
                .setItems(new String[]{"Present", "Absent"}, (dialog, which) -> {
                    String status = which == 0 ? "Present" : "Absent";
                    markAttendance(studentId, className, status);
                })
                .show();
        }
    }

    private void markAttendance(int studentId, String className, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(new Date());

        Cursor cursor = db.query("classes", new String[]{"id"}, 
            "name = ?", new String[]{className}, 
            null, null, null);

        if(cursor.moveToFirst()) {
            int classId = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put("class_id", classId);
            values.put("student_id", studentId);
            values.put("date", date);
            values.put("status", status);
            db.insert("attendance", null, values);
        }
        cursor.close();
    }

    private void viewAttendance() {
        String className = classSpinner.getSelectedItem().toString();
        ArrayList<String> attendance = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
            "SELECT u.username, a.date, a.status FROM attendance a " +
            "JOIN students s ON a.student_id = s.id " +
            "JOIN users u ON s.user_id = u.id " +
            "JOIN classes c ON a.class_id = c.id " +
            "WHERE c.name = ? ORDER BY a.date DESC",
            new String[]{className});

        while(cursor.moveToNext()) {
            String student = cursor.getString(0);
            String date = cursor.getString(1);
            String status = cursor.getString(2);
            attendance.add(student + " - " + date + ": " + status);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, attendance);
        listView.setAdapter(adapter);
    }
}