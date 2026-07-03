package br.edu.ifsulminas.mch.homeworkhelper.model.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;

public class SubjectDAO extends DAO {

    public SubjectDAO(Context context) {
        super(context);
    }

    public void save(Subject subject) {
        SQLiteDatabase db = openToWrite();

        ContentValues values = new ContentValues();
        values.put(TablesData.Subject.SUBJECT_NAME, subject.getName());
        values.put(TablesData.Subject.TEACHER, subject.getTeacher());
        values.put(TablesData.Subject.SCHOOL_YEAR, subject.getSchoolYear());
        values.put(TablesData.Subject.COLOR, subject.getColor());

        db.insert(TablesData.Subject.NAME, null, values);
        db.close();
    }

    public void update(Subject subject) {
        SQLiteDatabase db = openToWrite();

        ContentValues values = new ContentValues();
        values.put(TablesData.Subject.SUBJECT_NAME, subject.getName());
        values.put(TablesData.Subject.TEACHER, subject.getTeacher());
        values.put(TablesData.Subject.SCHOOL_YEAR, subject.getSchoolYear());
        values.put(TablesData.Subject.COLOR, subject.getColor());

        String[] params = {String.valueOf(subject.getId())};
        db.update(TablesData.Subject.NAME, values, TablesData.Subject.PK + "= ?", params);
        db.close();
    }

    public void delete(Subject subject) {
        SQLiteDatabase db = openToWrite();

        String[] params = {String.valueOf(subject.getId())};
        db.delete(TablesData.Subject.NAME, TablesData.Subject.PK + "= ?", params);
        db.close();
    }

    public List<Subject> listAll() {
        List<Subject> subjects = new ArrayList<>();

        SQLiteDatabase db = openToRead();
        String sql = "SELECT * FROM " + TablesData.Subject.NAME +
                " ORDER BY " + TablesData.Subject.PK + ";";

        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            Subject subject = new Subject();
            subject.setId(cursor.getInt(cursor.getColumnIndexOrThrow(TablesData.Subject.PK)));
            subject.setName(cursor.getString(cursor.getColumnIndexOrThrow(TablesData.Subject.SUBJECT_NAME)));
            subject.setTeacher(cursor.getString(cursor.getColumnIndexOrThrow(TablesData.Subject.TEACHER)));
            subject.setSchoolYear(cursor.getString(cursor.getColumnIndexOrThrow(TablesData.Subject.SCHOOL_YEAR)));
            subject.setColor(cursor.getString(cursor.getColumnIndexOrThrow(TablesData.Subject.COLOR)));

            subjects.add(subject);
        }

        cursor.close();
        db.close();
        return subjects;
    }
}