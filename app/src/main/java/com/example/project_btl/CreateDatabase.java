package com.example.project_btl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CreateDatabase extends SQLiteOpenHelper {

    // Tên CSDL và version
    public static final String DATABASE_NAME = "UserDB";
    public static final int DATABASE_VERSION = 1;

    // Tên bảng và cột
    public static final String TB_USER = "User";

    public static final String COL_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_FULLNAME = "fullname";
    public static final String COL_EMAIL = "email";

    public CreateDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // tạo bảng
        String createUserTable = "CREATE TABLE " + TB_USER + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_FULLNAME + " TEXT, " +
                COL_EMAIL + " TEXT)";
        db.execSQL(createUserTable);
    }
    // Thêm
    public boolean insertUser(String username, String password, String fullname, String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Kiểm tra username đã tồn tại chưa
        String checkQuery = "SELECT * FROM " + TB_USER + " WHERE " + COL_USERNAME + "=?";
        Cursor cursor = db.rawQuery(checkQuery, new String[]{username});
        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        values.put(COL_FULLNAME, fullname);
        values.put(COL_EMAIL, email);

        long result = db.insert(TB_USER, null, values);
        return result != -1;
    }
    //check
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TB_USER +
                " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    //up
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TB_USER);
        onCreate(db);
    }

    //<Lấy pass user
    public String getPassword(String username, String fullname, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String pass = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT " + COL_PASSWORD + " FROM " + TB_USER +
                            " WHERE " + COL_USERNAME + "=? AND " + COL_FULLNAME + "=? AND " + COL_EMAIL + "=?",
                    new String[]{username.trim(), fullname.trim(), email.trim()}
            );

            if (cursor != null && cursor.moveToFirst()) {
                pass = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return pass;
    }
}