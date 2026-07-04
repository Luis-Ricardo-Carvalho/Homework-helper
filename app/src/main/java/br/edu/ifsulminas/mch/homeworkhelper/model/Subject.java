package br.edu.ifsulminas.mch.homeworkhelper.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "subject")
public class Subject implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "teacher")
    private String teacher;

    @ColumnInfo(name = "school_year")
    private String schoolYear;

    @ColumnInfo(name = "color")
    private String color;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}