package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;
import br.edu.ifsulminas.mch.homeworkhelper.model.Task;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.AppDatabase;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.SubjectDao;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.TaskDao;

public class MainActivity extends AppCompatActivity {

    private ListView todoList;
    private Subject currentSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intentRecuperada = getIntent();
        if (intentRecuperada != null && intentRecuperada.hasExtra("SUBJECT_SELECTED")) {
            currentSubject = (Subject) intentRecuperada.getSerializableExtra("SUBJECT_SELECTED");
        }

        if (currentSubject != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentSubject.getName());
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent formIntent = new Intent(MainActivity.this, FormActivity.class);
            formIntent.putExtra("SUBJECT_FOR_TASK", currentSubject);
            startActivity(formIntent);
        });

        todoList = findViewById(R.id.todo_list);
        registerForContextMenu(todoList);

        todoList.setOnItemClickListener((parent, view, position, id) -> {
            Task task = (Task) todoList.getItemAtPosition(position);
            Intent formActIntent = new Intent(MainActivity.this, FormActivity.class);
            formActIntent.putExtra(FormActivity.TASK_KEY, task);
            formActIntent.putExtra("SUBJECT_FOR_TASK", currentSubject);
            startActivity(formActIntent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_edit_subject) {
            if (currentSubject != null) {
                Intent editSubjectIntent = new Intent(MainActivity.this, FormSubjectActivity.class);
                editSubjectIntent.putExtra(FormSubjectActivity.SUBJECT_KEY, currentSubject);
                startActivity(editSubjectIntent);
            } else {
                Toast.makeText(this, "Nenhuma matéria selecionada", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentSubject != null) {
            SubjectDao subjectDao = AppDatabase.getInstance(this).subjectDao();
            List<Subject> subjects = subjectDao.listAll();
            for (Subject s : subjects) {
                if (s.getId() == currentSubject.getId()) {
                    currentSubject = s;
                    break;
                }
            }

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(currentSubject.getName());
            }
        }

        updateTasksList();
    }

    private void updateTasksList() {
        TaskDao dao = AppDatabase.getInstance(this).taskDao();
        List<Task> tasks;

        if (currentSubject != null) {
            tasks = dao.listBySubject(currentSubject.getId());
        } else {
            tasks = dao.listAll();
        }

        TaskAdapter adapter = new TaskAdapter(this, tasks);
        todoList.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuItem itemDelete = menu.add("Concluir Tarefa");
        itemDelete.setOnMenuItemClickListener(item -> {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Task task = (Task) todoList.getItemAtPosition(info.position);

            AppDatabase.getInstance(MainActivity.this).taskDao().delete(task);
            updateTasksList();
            return true;
        });
    }
}