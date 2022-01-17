package com.example.diashield;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String USER_SYMPTOMS_TABLE = "user_symptoms";
    public static final String COL_HEART_RATE = "heartRate";
    public static final String COL_RES_RATE = "respRate";
    public static final String COL_HEADACHE = "headache";
    public static final String COL_FEVER = "fever";
    public static final String COL_NAUSEA = "nausea";
    public static final String COL_DIARRHEA = "diarrhea";
    public static final String COL_SORE_THROAT = "soreThroat";
    public static final String COL_MUSCLE_PAIN = "musclePain";
    public static final String COL_COUGH = "cough";
    public static final String COL_LOSS_OF_SMELL_OR_TASTE = "lossOfSmellTaste";
    public static final String COL_SHORTNESS_OF_BREATH = "shortnessOfBreath";
    public static final String COL_FATIGUE = "fatigue";

    public static final String COL_ROW_ID = "rowId";

    public DatabaseHelper(Context context, String Name) {
        super(context, "Gupta.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("DiaShield", "Now creating db");
        String createQuery = "CREATE TABLE " + USER_SYMPTOMS_TABLE + "(" +
                COL_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HEART_RATE +" REAL, " +
                COL_RES_RATE+ " REAL, "+
                COL_HEADACHE + " REAL, " +
                COL_FEVER + " REAL, " +
                COL_NAUSEA + " REAL, " +
                COL_DIARRHEA + " REAL, " +
                COL_SORE_THROAT + " REAL, " +
                COL_MUSCLE_PAIN + " REAL, " +
                COL_COUGH + " REAL, "+
                COL_LOSS_OF_SMELL_OR_TASTE + " REAL, " +
                COL_SHORTNESS_OF_BREATH + " REAL, "+
                COL_FATIGUE + " REAL " + ");";

        db.execSQL(createQuery);
    }

    public long addRow(com.example.diashield.User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentVal = new ContentValues();
        contentVal.put(COL_HEART_RATE, user.heartrate);
        contentVal.put(COL_RES_RATE, user.resprate);
        contentVal.put(COL_HEADACHE, 0);
        contentVal.put(COL_FEVER, 0);
        contentVal.put(COL_NAUSEA, 0);
        contentVal.put(COL_DIARRHEA, 0);
        contentVal.put(COL_SORE_THROAT, 0);
        contentVal.put(COL_MUSCLE_PAIN, 0);
        contentVal.put(COL_COUGH, 0);
        contentVal.put(COL_LOSS_OF_SMELL_OR_TASTE, 0);
        contentVal.put(COL_SHORTNESS_OF_BREATH, 0);
        contentVal.put(COL_FATIGUE, 0);

        long rowId = db.insert(USER_SYMPTOMS_TABLE, null, contentVal);

        return rowId;
    }

    public int updateRow(com.example.diashield.User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentVal = new ContentValues();
        contentVal.put(COL_HEART_RATE, user.heartrate);
        contentVal.put(COL_RES_RATE, user.resprate);
        contentVal.put(COL_HEADACHE, 0);
        contentVal.put(COL_FEVER, 0);
        contentVal.put(COL_NAUSEA, 0);
        contentVal.put(COL_DIARRHEA, 0);
        contentVal.put(COL_SORE_THROAT, 0);
        contentVal.put(COL_MUSCLE_PAIN, 0);
        contentVal.put(COL_COUGH, 0);
        contentVal.put(COL_LOSS_OF_SMELL_OR_TASTE, 0);
        contentVal.put(COL_SHORTNESS_OF_BREATH, 0);
        contentVal.put(COL_FATIGUE, 0);

        int rowId = db.update(USER_SYMPTOMS_TABLE, contentVal, COL_ROW_ID + " = ?", new String[]{String.valueOf(user.id)});

        return rowId;
    }

    public boolean updateRow(ContentValues contentVal, long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.update(USER_SYMPTOMS_TABLE, contentVal, COL_ROW_ID + " = ?", new String[]{String.valueOf(rowId)});
        db.close();

        if (result == -1) {
            return  false;
        } else {
            return true;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
