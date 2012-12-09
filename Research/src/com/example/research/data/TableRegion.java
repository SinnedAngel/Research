package com.example.research.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableRegion
{
    public static final String TABLE_NAME = "regions";

    public static final String ID = "_id";
    public static final String CODE = "code";
    public static final String TITLE = "title";

    private static final String SQL_CREATE[] = {
        "CREATE TABLE " + TABLE_NAME + " (" 
                + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," 
                + CODE + " INTEGER NOT NULL," 
                + TITLE + " text(150,0) NOT NULL" + ");",
        "CREATE INDEX " + TABLE_NAME + CODE + "_idx ON " + TABLE_NAME + " (" + CODE + " ASC);",
        "CREATE INDEX " + TABLE_NAME + TITLE + "_idx ON " + TABLE_NAME + " (" + TITLE + " COLLATE NOCASE ASC);" 
    };

    public static void onCreate(SQLiteDatabase db)
    {
        db.beginTransaction();
        try
        {
            for (String sql : SQL_CREATE)
            {
                Log.i(TableRegion.class.getName(), sql);
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            Log.e(TableRegion.class.getName(), e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.i(TableRegion.class.getName(), "Upgrade " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }
}
