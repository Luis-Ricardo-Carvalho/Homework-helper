package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.SubjectDAO;

public class IndexActivity extends AppCompatActivity {

    private ListView subject_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_index);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.index), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent formIntent = new Intent(IndexActivity.this, FormSubjectActivity.class);
            startActivity(formIntent);
        });

        subject_list = findViewById(R.id.subject_list);
        registerForContextMenu(subject_list);

        subject_list.setOnItemClickListener((parent, view, position, id) -> {
            Subject subject = (Subject) subject_list.getItemAtPosition(position);

            Intent formActIntent = new Intent(IndexActivity.this, FormSubjectActivity.class);
            formActIntent.putExtra(FormSubjectActivity.SUBJECT_KEY, subject);
            startActivity(formActIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSubjectList();
    }

    private void updateSubjectList() {
        SubjectDAO dao = new SubjectDAO(this);
        List<Subject> subjects = dao.listAll();

        ArrayAdapter<Subject> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, subjects);
        subject_list.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuItem itemDelete = menu.add("Excluir Disciplina");

        itemDelete.setOnMenuItemClickListener(item -> {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Subject subject = (Subject) subject_list.getItemAtPosition(info.position);

            SubjectDAO dao = new SubjectDAO(IndexActivity.this);
            dao.delete(subject);
            updateSubjectList();
            return true;
        });
    }
}