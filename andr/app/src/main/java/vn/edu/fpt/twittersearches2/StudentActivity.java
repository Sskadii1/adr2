package vn.edu.fpt.twittersearches2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import java.util.ArrayList;

public class StudentActivity extends Activity {
    private DatabaseHelper dbHelper;
    private ListView listView;
    private int studentId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        
        studentId = getIntent().getIntExtra("USER_ID", -1);
        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.listView);
        
        loadAttendance();
    }
    
    private void loadAttendance() {
        ArrayList<String> attendance = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT c.name, a.date, a.status FROM attendance a " +
            "JOIN classes c ON a.class_id = c.id " +
            "WHERE a.student_id = ?", 
            new String[]{String.valueOf(studentId)});
        
        while(cursor.moveToNext()) {
            String className = cursor.getString(0);
            String date = cursor.getString(1);
            String status = cursor.getString(2);
            attendance.add(className + " - " + date + ": " + status);
        }
        cursor.close();
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, attendance);
        listView.setAdapter(adapter);
    }
}


