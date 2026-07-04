package br.edu.ifsulminas.mch.homeworkhelper;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.AppDatabase;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.SubjectDao;

public class FormSubjectActivity extends AppCompatActivity {

    public static final String SUBJECT_KEY = "subject";
    private Subject subject = null;

    private EditText nameEditText;
    private EditText teacherEditText;
    private EditText schoolYearEditText;
    private Spinner colorSpinner;

    private final String[] colorNames = {"Vermelho", "Rosa", "Roxo", "Azul", "Verde-água", "Verde", "Laranja", "Amarelo"};
    private final String[] colorKeys  = {"subject_red", "subject_pink", "subject_purple", "subject_blue", "subject_teal", "subject_green", "subject_orange", "subject_yellow"};

    private String selectedColorKey = "subject_blue";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_subject);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.form_subject), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameEditText       = findViewById(R.id.subject_name);
        teacherEditText    = findViewById(R.id.subject_teacher);
        schoolYearEditText = findViewById(R.id.subject_schoolYear);
        colorSpinner       = findViewById(R.id.subject_color_spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                colorNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(spinnerAdapter);

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedColorKey = colorKeys[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        subject = (Subject) getIntent().getSerializableExtra(SUBJECT_KEY);
        if (subject != null) {
            nameEditText.setText(subject.getName());
            teacherEditText.setText(subject.getTeacher());
            schoolYearEditText.setText(subject.getSchoolYear());

            for (int i = 0; i < colorKeys.length; i++) {
                if (colorKeys[i].equals(subject.getColor())) {
                    colorSpinner.setSelection(i);
                    break;
                }
            }
        }

        nameEditText.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_form_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_edit_subject) {
            String name       = nameEditText.getText().toString();
            String teacher    = teacherEditText.getText().toString();
            String schoolYear = schoolYearEditText.getText().toString();

            if (name.isBlank()) {
                Toast.makeText(this, "O nome da Disciplina não pode ser vazio", Toast.LENGTH_SHORT).show();
            } else if (teacher.isBlank()) {
                Toast.makeText(this, "O Professor da Disciplina não pode ser vazio", Toast.LENGTH_SHORT).show();
            } else if (schoolYear.isBlank()) {
                Toast.makeText(this, "A Escolaridade não pode ser vazia", Toast.LENGTH_SHORT).show();
            } else {
                boolean isInsert = (subject == null);
                if (isInsert) subject = new Subject();

                subject.setName(name);
                subject.setTeacher(teacher);
                subject.setSchoolYear(schoolYear);
                subject.setColor(selectedColorKey);

                SubjectDao dao = AppDatabase.getInstance(this).subjectDao();
                if (isInsert) dao.save(subject);
                else dao.update(subject);

                Toast.makeText(this, "Disciplina salva com sucesso", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}