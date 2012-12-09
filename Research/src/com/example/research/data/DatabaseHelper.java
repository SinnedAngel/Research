package com.example.research.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "tokobagus.db";
    private static final int DB_VERSION = 1;
    
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        TableCategory.onCreate(db);
        TableRegion.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        TableCategory.onUpgrade(db, oldVersion, newVersion);
        TableRegion.onUpgrade(db, oldVersion, newVersion);
        onCreate(db);
    }

}
