package br.edu.ifsulminas.mch.homeworkhelper.model.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.edu.ifsulminas.mch.homeworkhelper.model.Task;

@Dao
public interface TaskDao {

    @Insert
    void save(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY id")
    List<Task> listAll();

    @Query("SELECT * FROM tasks WHERE subject_id = :subjectId ORDER BY id")
    List<Task> listBySubject(int subjectId);
}