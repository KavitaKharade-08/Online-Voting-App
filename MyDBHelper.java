package com.example.omg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "userdata.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_VOTER = "Voter";

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Voter table
        db.execSQL("CREATE TABLE " + TABLE_VOTER + " (" +
                "VoterId TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "contact TEXT, " +
                "dob TEXT, " +
                "Email TEXT, " +
                "face BLOB)");

        // Vote table (name used directly)
        db.execSQL("CREATE TABLE Vote (" +
                "voterId TEXT PRIMARY KEY, " +
                "partyName TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If there's a version upgrade, drop and recreate tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOTER);
        db.execSQL("DROP TABLE IF EXISTS Vote");
        onCreate(db);
    }

    // ==== VOTER TABLE METHODS ====

    public boolean insertuserdata(String id, String name, String contact, String dob, String mail, byte[] faceImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("VoterId", id);
        cv.put("name", name);
        cv.put("contact", contact);
        cv.put("dob", dob);
        cv.put("Email", mail);
        cv.put("face", faceImage);

        long result = db.insert(TABLE_VOTER, null, cv);
        db.close();
        return result != -1;
    }

    public boolean insertuserdata(String id, String name, String contact, String dob, String mail) {
        return insertuserdata(id, name, contact, dob, mail, null);
    }

    public boolean checkUser(String email, String voterId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_VOTER + " WHERE Email = ? AND VoterId = ?", new String[]{email, voterId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean updateuserdata(String voterId, String contact, String dob) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("contact", contact);
        cv.put("dob", dob);

        int result = db.update(TABLE_VOTER, cv, "VoterId = ?", new String[]{voterId});
        db.close();
        return result > 0;
    }

    public boolean deleteuserdata(String voterId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_VOTER, "VoterId = ?", new String[]{voterId});
        db.close();
        return result > 0;
    }

    public Cursor getdata() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_VOTER, null);
    }

    public Cursor getVoterById(String voterId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_VOTER + " WHERE VoterId = ?", new String[]{voterId});
    }

    // ==== VOTE TABLE METHODS (uses "Vote" directly) ====

    public boolean insertVoteRecord(String voterId, String partyName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("voterId", voterId);
        cv.put("partyName", partyName);

        long result = db.insert("Vote", null, cv);
        db.close();
        return result != -1;
    }

    public int getTotalVoterCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Vote", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public String getMobileNumber(String voterId) {
        String mobile = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT contact FROM Voter WHERE VoterId = ?", new String[]{voterId});

        if (cursor != null && cursor.moveToFirst()) {
            mobile = cursor.getString(0);  // 'contact' is the mobile number
            cursor.close();
        }

        db.close();
        return mobile;
    }

    // Check if the voter has already voted
    public boolean hasVoted(String voterId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Vote WHERE voterId = ?", new String[]{voterId});
        boolean hasVoted = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return hasVoted;
    }


}
