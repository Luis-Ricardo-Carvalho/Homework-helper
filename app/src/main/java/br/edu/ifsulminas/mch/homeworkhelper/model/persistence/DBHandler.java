package br.edu.ifsulminas.mch.homeworkhelper.model.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "tarefas.db";
    private static final int DB_VERSION = 3;

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TablesData.Subject.CREATE_SQL);
        db.execSQL(TablesData.Tasks.CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TablesData.Tasks.NAME +
                    " ADD COLUMN " + TablesData.Tasks.CALENDAR_EVENT_ID + " VARCHAR(100)");
        }
    }
}