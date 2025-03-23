
package vn.edu.fpt.twittersearches2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ClassroomDB";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, password TEXT, role TEXT)");
        
        // Classes table with foreign key to teacher
        db.execSQL("CREATE TABLE classes (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, teacher_id INTEGER, " +
                "FOREIGN KEY(teacher_id) REFERENCES users(id))");
        
        // Students table with foreign keys
        db.execSQL("CREATE TABLE students (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "class_id INTEGER, user_id INTEGER, " +
                "FOREIGN KEY(class_id) REFERENCES classes(id), " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
        
        // Attendance table with foreign keys
        db.execSQL("CREATE TABLE attendance (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "class_id INTEGER, student_id INTEGER, date TEXT, status TEXT, " +
                "FOREIGN KEY(class_id) REFERENCES classes(id), " +
                "FOREIGN KEY(student_id) REFERENCES students(id))");

        // Insert default admin
        ContentValues adminValues = new ContentValues();
        adminValues.put("username", "admin");
        adminValues.put("password", "admin123");
        adminValues.put("role", "admin");
        db.insert("users", null, adminValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS classes"); 
        db.execSQL("DROP TABLE IF EXISTS students");
        db.execSQL("DROP TABLE IF EXISTS attendance");
        onCreate(db);
    }
}
