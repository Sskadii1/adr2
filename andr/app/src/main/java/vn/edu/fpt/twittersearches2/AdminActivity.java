package vn.edu.fpt.twittersearches2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;
import android.app.AlertDialog;
import java.util.ArrayList;

public class AdminActivity extends Activity {
    private DatabaseHelper dbHelper;
    private EditText nameEdit, usernameEdit, passwordEdit;
    private Spinner roleSpinner, classSpinner, teacherSpinner;
    private ListView listView;
    private ArrayList<String> classNames;
    private ArrayList<Integer> classIds;
    private ArrayList<String> teacherNames;
    private ArrayList<Integer> teacherIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);

        nameEdit = findViewById(R.id.nameEdit);
        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        roleSpinner = findViewById(R.id.roleSpinner);
        classSpinner = findViewById(R.id.classSpinner);
        teacherSpinner = findViewById(R.id.teacherSpinner);
        listView = findViewById(R.id.listView);

        Button addClass = findViewById(R.id.addClassButton);
        Button addUser = findViewById(R.id.addUserButton);
        Button viewClasses = findViewById(R.id.viewClassesButton);
        Button viewUsers = findViewById(R.id.viewUsersButton);

        // Load classes and teachers into spinners
        loadClassesIntoSpinner();
        loadTeachersIntoSpinner();

        addClass.setOnClickListener(v -> addClass());
        addUser.setOnClickListener(v -> addUser());
        viewClasses.setOnClickListener(v -> loadClasses());
        viewUsers.setOnClickListener(v -> loadUsers());
    }

    private void loadClassesIntoSpinner() {
        classNames = new ArrayList<>();
        classIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("classes", new String[]{"id", "name"},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            classIds.add(id);
            classNames.add(name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, classNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(adapter);
    }

    private void loadTeachersIntoSpinner() {
        teacherNames = new ArrayList<>();
        teacherIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id", "username"},
                "role = ?", new String[]{"lecturer"}, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String username = cursor.getString(1);
            teacherIds.add(id);
            teacherNames.add(username);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teacherNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teacherSpinner.setAdapter(adapter);
    }

    private void addClass() {
        String name = nameEdit.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên lớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (teacherSpinner.getAdapter().getCount() == 0) {
            Toast.makeText(this, "Chưa có giảng viên nào để gán cho lớp", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedTeacherPosition = teacherSpinner.getSelectedItemPosition();
        int teacherId = teacherIds.get(selectedTeacherPosition);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("teacher_id", teacherId);
        long result = db.insert("classes", null, values);
        if (result == -1) {
            Toast.makeText(this, "Lỗi khi thêm lớp", Toast.LENGTH_SHORT).show();
            return;
        }
        loadClasses();
        loadClassesIntoSpinner();
        Toast.makeText(this, "Thêm lớp thành công", Toast.LENGTH_SHORT).show();
    }

    private void addUser() {
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Kiểm tra username trùng lặp
        Cursor cursor = db.query("users", new String[]{"id"},
                "username = ?", new String[]{username}, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);
        long userId = db.insert("users", null, values);

        if (userId == -1) {
            Toast.makeText(this, "Lỗi khi thêm người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (role.equals("student")) {
            if (classSpinner.getAdapter().getCount() == 0) {
                Toast.makeText(this, "Chưa có lớp nào để gán sinh viên", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = classSpinner.getSelectedItemPosition();
            int classId = classIds.get(selectedPosition);
            values = new ContentValues();
            values.put("user_id", userId);
            values.put("class_id", classId);
            db.insert("students", null, values);
        }
        loadUsers();
        Toast.makeText(this, "Thêm người dùng thành công", Toast.LENGTH_SHORT).show();
    }

    private void loadClasses() {
        ArrayList<String> classes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("classes", new String[]{"id", "name", "teacher_id"},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            int teacherId = cursor.getInt(2);
            String teacherName = getTeacherName(teacherId);
            classes.add(id + ": " + name + " (Giảng viên: " + teacherName + ")");
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, classes);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selected = classes.get(position);
            int classId = Integer.parseInt(selected.split(":")[0]);
            showClassOptions(classId);
            return true;
        });
    }

    private String getTeacherName(int teacherId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"username"},
                "id = ?", new String[]{String.valueOf(teacherId)}, null, null, null);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }
        cursor.close();
        return "Không xác định";
    }

    private void loadUsers() {
        ArrayList<String> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id", "username", "role"},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String username = cursor.getString(1);
            String role = cursor.getString(2);
            users.add(id + ": " + username + " (" + role + ")");
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, users);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selected = users.get(position);
            int userId = Integer.parseInt(selected.split(":")[0]);
            showUserOptions(userId);
            return true;
        });
    }

    private void showClassOptions(int classId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = {"Edit", "Delete"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                editClass(classId);
            } else {
                deleteClass(classId);
            }
        });
        builder.show();
    }

    private void showUserOptions(int userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = {"Edit", "Delete"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                editUser(userId);
            } else {
                deleteUser(userId);
            }
        });
        builder.show();
    }

    private void editClass(int classId) {
        String newName = nameEdit.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên lớp mới", Toast.LENGTH_SHORT).show();
            return;
        }

        if (teacherSpinner.getAdapter().getCount() == 0) {
            Toast.makeText(this, "Chưa có giảng viên nào để gán cho lớp", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedTeacherPosition = teacherSpinner.getSelectedItemPosition();
        int teacherId = teacherIds.get(selectedTeacherPosition);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("teacher_id", teacherId);
        int rowsAffected = db.update("classes", values, "id = ?",
                new String[]{String.valueOf(classId)});
        if (rowsAffected == 0) {
            Toast.makeText(this, "Lỗi khi sửa lớp", Toast.LENGTH_SHORT).show();
            return;
        }
        loadClasses();
        loadClassesIntoSpinner();
        Toast.makeText(this, "Sửa lớp thành công", Toast.LENGTH_SHORT).show();
    }

    private void deleteClass(int classId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("classes", "id = ?",
                new String[]{String.valueOf(classId)});
        loadClasses();
        loadClassesIntoSpinner();
        Toast.makeText(this, "Xóa lớp thành công", Toast.LENGTH_SHORT).show();
    }

    private void editUser(int userId) {
        String newUsername = usernameEdit.getText().toString().trim();
        String newPassword = passwordEdit.getText().toString().trim();
        String newRole = roleSpinner.getSelectedItem().toString();

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Kiểm tra username trùng lặp (trừ chính user đang sửa)
        Cursor cursor = db.query("users", new String[]{"id"},
                "username = ? AND id != ?", new String[]{newUsername, String.valueOf(userId)}, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        values.put("password", newPassword);
        values.put("role", newRole);
        int rowsAffected = db.update("users", values, "id = ?",
                new String[]{String.valueOf(userId)});
        if (rowsAffected == 0) {
            Toast.makeText(this, "Lỗi khi sửa người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        loadUsers();
        Toast.makeText(this, "Sửa người dùng thành công", Toast.LENGTH_SHORT).show();
    }

    private void deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("users", "id = ?",
                new String[]{String.valueOf(userId)});
        loadUsers();
        Toast.makeText(this, "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
    }
}