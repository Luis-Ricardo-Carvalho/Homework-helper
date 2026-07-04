package br.edu.ifsulminas.mch.homeworkhelper.model.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;

@Dao
public interface SubjectDao {

    @Insert
    void save(Subject subject);

    @Update
    void update(Subject subject);

    @Delete
    void delete(Subject subject);

    @Query("SELECT * FROM subject ORDER BY id")
    List<Subject> listAll();
}