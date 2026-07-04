package br.edu.ifsulminas.mch.homeworkhelper.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "tasks",
        foreignKeys = @ForeignKey(
                entity = Subject.class,
                parentColumns = "id",
                childColumns = "subject_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "date_submission")
    private String dateSubmission;

    @ColumnInfo(name = "active")
    private boolean active;

    @ColumnInfo(name = "subject_id", index = true)
    private int subjectId;

    @ColumnInfo(name = "calendar_event_id")
    private String calendarEventId;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDateSubmission() { return dateSubmission; }
    public void setDateSubmission(String dateSubmission) { this.dateSubmission = dateSubmission; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getCalendarEventId() { return calendarEventId; }
    public void setCalendarEventId(String calendarEventId) { this.calendarEventId = calendarEventId; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}