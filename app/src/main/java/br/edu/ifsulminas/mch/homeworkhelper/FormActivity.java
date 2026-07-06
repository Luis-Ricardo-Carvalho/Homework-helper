package br.edu.ifsulminas.mch.homeworkhelper;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;
import br.edu.ifsulminas.mch.homeworkhelper.model.Task;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.AppDatabase;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.TaskDao;
import android.provider.CalendarContract;

public class FormActivity extends AppCompatActivity {

    public static final String TASK_KEY = "tarefa";
    private Task task = null;
    private Subject currentSubject = null;
    private EditText nameEditText;
    private EditText descEditText;
    private Button btnDate;
    private String selectedDateISO = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.form), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SUBJECT_FOR_TASK")) {
            currentSubject = (Subject) intent.getSerializableExtra("SUBJECT_FOR_TASK");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameEditText = findViewById(R.id.task_name);
        descEditText = findViewById(R.id.task_description);
        btnDate = findViewById(R.id.btn_date);

        if (intent != null && intent.hasExtra(TASK_KEY)) {
            task = (Task) intent.getSerializableExtra(TASK_KEY);
        }

        if (task != null) {
            nameEditText.setText(task.getName());
            descEditText.setText(task.getDescription());
            if (task.getDateSubmission() != null && !task.getDateSubmission().isEmpty()) {
                selectedDateISO = task.getDateSubmission();
                btnDate.setText(formatDateDisplay(selectedDateISO));
            }
        }

        btnDate.setOnClickListener(v -> showDatePicker());
        nameEditText.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_form_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_edit_subject) {
            String name = nameEditText.getText().toString();
            String desc = descEditText.getText().toString();

            if (name.isBlank() || desc.isBlank() || selectedDateISO == null || selectedDateISO.isBlank()) {
                Toast.makeText(getBaseContext(), "Por favor, preencha todos os campos da tarefa.", Toast.LENGTH_SHORT).show();
            } else {
                boolean isInsert = (task == null);
                if (isInsert) task = new Task();

                task.setName(name);
                task.setDescription(desc);
                task.setDateSubmission(selectedDateISO);
                task.setActive(true);

                if (isInsert && currentSubject != null) {
                    task.setSubjectId(currentSubject.getId());
                }

                TaskDao dao = AppDatabase.getInstance(this).taskDao();
                if (isInsert) dao.save(task);
                else dao.update(task);

                Toast.makeText(getBaseContext(), "Tarefa salva com sucesso", Toast.LENGTH_SHORT).show();
                abrirCalendario(task);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDateISO != null && !selectedDateISO.isEmpty()) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = isoFormat.parse(selectedDateISO);
                calendar.setTime(date);
            } catch (ParseException e) {}
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDateISO = isoFormat.format(selected.getTime());
                    btnDate.setText(formatDateDisplay(selectedDateISO));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private String formatDateDisplay(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = isoFormat.parse(isoDate);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return displayFormat.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    private void abrirCalendario(Task task) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = format.parse(task.getDateSubmission());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            Intent intent;
            if (task.getCalendarEventId() == null || task.getCalendarEventId().isEmpty()) {
                intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
            } else {
                intent = new Intent(Intent.ACTION_EDIT);
                intent.setData(CalendarContract.Events.CONTENT_URI.buildUpon()
                        .appendPath(task.getCalendarEventId()).build());
            }

            intent.putExtra(CalendarContract.Events.TITLE, task.getName());
            intent.putExtra(CalendarContract.Events.DESCRIPTION, task.getDescription());
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.getTimeInMillis());

            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir calendário", Toast.LENGTH_SHORT).show();
        }
    }
}