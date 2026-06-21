package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.SubjectDAO;

public class IndexActivity extends AppCompatActivity {

    private RecyclerView subject_list;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent formIntent = new Intent(IndexActivity.this, FormSubjectActivity.class);
            startActivity(formIntent);
        });

        subject_list = findViewById(R.id.subject_list);

        subject_list.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSubjectList();
    }

    private void updateSubjectList() {
        SubjectDAO dao = new SubjectDAO(this);
        List<Subject> subjects = dao.listAll();

        SubjectAdapter adapter = new SubjectAdapter(subjects, subject -> {
            Intent mainActIntent = new Intent(IndexActivity.this, MainActivity.class);

            mainActIntent.putExtra("SUBJECT_SELECTED", subject);

            startActivity(mainActIntent);
        });

        subject_list.setAdapter(adapter);
    }
}