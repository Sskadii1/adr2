
package vn.edu.fpt.twittersearches2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText usernameEdit, passwordEdit;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id", "role"},
                "username = ? AND password = ?",
                new String[]{username, password},
                null, null, null);

        if (cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndex("role"));
            int userId = cursor.getInt(cursor.getColumnIndex("id"));

            Intent intent;
            switch (role) {
                case "admin":
                    intent = new Intent(this, AdminActivity.class);
                    break;
                case "teacher":
                    intent = new Intent(this, TeacherActivity.class);
                    break;
                default:
                    intent = new Intent(this, StudentActivity.class);
                    break;
            }
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }
}
