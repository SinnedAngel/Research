package com.example.research.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableCategory
{
    public static final String TABLE_NAME = "categories";

    public static final String ID = "_id";
    public static final String CODE = "code";
    public static final String LEVEL = "level";
    public static final String TYPE = "type";
    public static final String PARENT = "parent";
    public static final String TITLE = "title";

    /**
     * possible usage queries 
     * 1. select fields from table where level=1 order by title asc 
     * 2. select fields from table where parent={id} and level=2 order by title asc 
     * 3. select fields from table where parent={id} and level=3 order by title asc 
     * 4. select fields from table where code={code}
     */
    private static final String SQL_CREATE[] = {
        "CREATE TABLE " + TABLE_NAME + " (" 
                + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," 
                + CODE + " INTEGER NOT NULL," 
                + LEVEL + " INTEGER NOT NULL," 
                + TYPE + " INTEGER NOT NULL,"
                + PARENT + " INTEGER NOT NULL,"
                + TITLE + " text(150,0) NOT NULL" + ");",
        "CREATE INDEX " + TABLE_NAME + LEVEL + "_idx ON " + TABLE_NAME + " (" + LEVEL + " ASC);",
        "CREATE INDEX " + TABLE_NAME + PARENT + "_" + LEVEL + "_idx ON " + TABLE_NAME + " (" + PARENT + ", " + LEVEL + ");",
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
                Log.i(TableCategory.class.getName(), sql);
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            Log.e(TableCategory.class.getName(), e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            db.endTransaction();
        }
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.i(TableCategory.class.getName(), "Upgrade " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }
}
