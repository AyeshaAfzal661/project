package com.valdioveliu.valdio.audioplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB_Helper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TABLE_SONGS.db";
    public DB_Helper(Context context){ super(context,DATABASE_NAME,null,DATABASE_VERSION);}
    public void onCreate(SQLiteDatabase db){
        String sql = "CREATE TABLE TABLE_SONGS (data TEXT, " +
                "title TEXT," +
                "album TEXT,"+
                "artist TEXT)";

        db.execSQL(sql);


    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TABLE_SONGS");
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db,oldVersion,newVersion);
    }
}
