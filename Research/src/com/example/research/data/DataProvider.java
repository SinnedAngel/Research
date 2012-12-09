package com.example.research.data;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DataProvider extends ContentProvider
{
    private DatabaseHelper mDatabase;
    
    private static final int CAT = 10;
    private static final int CAT_ID = 11;
    private static final int REG = 12;
    private static final int REG_ID = 13;
    
    private static final String AUTHORITY = "com.example.research.data.DataProvider";
    public static final Uri CONTENT_URI_CAT =  Uri.parse("content://"+AUTHORITY+"/cat");
    public static final Uri CONTENT_URI_REG =  Uri.parse("content://"+AUTHORITY+"/reg");
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        mUriMatcher.addURI(AUTHORITY, "cat", CAT);
        mUriMatcher.addURI(AUTHORITY, "cat/#", CAT_ID);
        mUriMatcher.addURI(AUTHORITY, "reg", REG);
        mUriMatcher.addURI(AUTHORITY, "reg/#", REG_ID);
    }
    
    private static final String TAG_LOG = "data.provider";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int nd = 0;
        String id;
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        int sUri = mUriMatcher.match(uri);
        switch(sUri) {
            case CAT:
                nd = db.delete(TableCategory.TABLE_NAME, selection, selectionArgs);
                break;
            case CAT_ID:
                id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    nd = db.delete(TableCategory.TABLE_NAME, TableCategory.ID+"="+id,null);
                }
                else{
                    nd = db.delete(TableCategory.TABLE_NAME, TableCategory.ID+"="+id+" and "+selection,selectionArgs);
                }
                break;
            case REG:
                nd = db.delete(TableRegion.TABLE_NAME, selection, selectionArgs);
                break;
            case REG_ID:
                id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)) {
                    nd = db.delete(TableRegion.TABLE_NAME, TableRegion.ID+"="+id,null);
                }
                else{
                    nd = db.delete(TableRegion.TABLE_NAME, TableRegion.ID+"="+id+" and "+selection,selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown URI: "+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return nd;
    }

    @Override
    public String getType(Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        int sUri = mUriMatcher.match(uri);
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        long id = 0;
        String url;
        switch(sUri) {
            case CAT:
                id = db.insert(TableCategory.TABLE_NAME,null,values);
                url = "cat/"+id;
                break;
            case REG:
                id = db.insert(TableRegion.TABLE_NAME,null,values);
                url = "reg/"+id;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: "+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(url);
    }

    @Override
    public boolean onCreate()
    {
        mDatabase = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
        String table;
        int sUri = mUriMatcher.match(uri);
        switch(sUri) {
            case CAT:
                table = TableCategory.TABLE_NAME;
                break;
            case CAT_ID:
                table = TableCategory.TABLE_NAME;
                sqb.appendWhere(TableCategory.ID + "=" + uri.getLastPathSegment());
                break;
            case REG:
                table = TableRegion.TABLE_NAME;
                break;
            case REG_ID:
                table = TableRegion.TABLE_NAME;
                sqb.appendWhere(TableRegion.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("unknown URI: "+uri);
        }
        validateColumns(projection,table);
        sqb.setTables(table);
        
        SQLiteDatabase db = mDatabase.getReadableDatabase();
        Cursor c = sqb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        String id;
        int total = 0;
        int sUri = mUriMatcher.match(uri);
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        switch(sUri) {
            case CAT:
                total = db.update(TableCategory.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CAT_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    total = db.update(TableCategory.TABLE_NAME, values, TableCategory.ID + "=" + id, null);
                } else {
                    total = db.update(TableCategory.TABLE_NAME, values, TableCategory.ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case REG:
                total = db.update(TableRegion.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REG_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    total = db.update(TableRegion.TABLE_NAME, values, TableRegion.ID + "=" + id, null);
                } else {
                    total = db.update(TableRegion.TABLE_NAME, values, TableRegion.ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return total;
    }
    
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        int sUri = mUriMatcher.match(uri);
        String table;
        switch(sUri) {
            case CAT:
                table = TableCategory.TABLE_NAME;
                break;
            case REG:
                table = TableRegion.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        db.beginTransaction();
        try {
            for(ContentValues cv : values) {
                long id = db.insertOrThrow(table,null,cv);
                if(id<1) {
                    throw new SQLException("Failed insert to "+uri);
                }
            }
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri,null);
        }
        catch(Exception e) {
            Log.e(TAG_LOG,"failed insert data reason: "+e.getMessage());
        }
        finally {
            db.endTransaction();
        }
        return values.length;
    }

    private void validateColumns(String[] projection, String table) {
        String[] columns;
        if(table.equals(TableCategory.TABLE_NAME)) {
            String[] col = {
                TableCategory.ID,
                TableCategory.CODE,
                TableCategory.LEVEL,
                TableCategory.TYPE,
                TableCategory.PARENT,
                TableCategory.TITLE
            };
            columns = col;
        }
        else if(table.equals(TableRegion.TABLE_NAME)) {
            String[] col = {
                    TableRegion.ID,
                    TableRegion.CODE,
                    TableRegion.TITLE
                };
                columns = col;
        }
        else {
            throw new IllegalArgumentException("Unknown table "+table);
        }
        if (projection != null) {
            HashSet<String> askedCols = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> realCols = new HashSet<String>(Arrays.asList(columns));
            if (!realCols.containsAll(askedCols)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}
