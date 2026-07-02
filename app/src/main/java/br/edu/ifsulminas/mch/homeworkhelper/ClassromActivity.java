package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.CourseWork;
import com.google.api.services.classroom.model.ListCoursesResponse;
import com.google.api.services.classroom.model.ListStudentSubmissionsResponse;
import com.google.api.services.classroom.model.StudentSubmission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassromActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;

    private ListView lvTarefas;
    private ArrayList<String> listaTarefasTexto;
    private ArrayAdapter<String> adapter;

    // Definição dos escopos necessários (Ver turmas e ver tarefas)
    private static final List<String> SCOPES = Arrays.asList(
            ClassroomScopes.CLASSROOM_COURSES_READONLY,
            ClassroomScopes.CLASSROOM_COURSEWORK_ME_READONLY
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classrom);

        Button btnLogin = findViewById(R.id.btn_login);
        lvTarefas = findViewById(R.id.lv_tarefas);

        listaTarefasTexto = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaTarefasTexto);
        lvTarefas.setAdapter(adapter);

        // CORRIGIDO: Agora pedindo os escopos específicos do Classroom
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(ClassroomScopes.CLASSROOM_COURSES_READONLY),
                        new Scope(ClassroomScopes.CLASSROOM_COURSEWORK_ME_READONLY))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> iniciarFluxoLogin());
    }

    private void iniciarFluxoLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Toast.makeText(this, "Conectado com sucesso!", Toast.LENGTH_SHORT).show();
                    buscarDadosClassroom(account);
                }
            } catch (ApiException e) {
                Log.e("ClassroomApp", "Erro no login Google: " + e.getStatusCode());
                Toast.makeText(this, "Falha na autenticação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void buscarDadosClassroom(GoogleSignInAccount account) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // CORRIGIDO: Passando a lista de escopos corretos para a credencial
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        ClassromActivity.this,
                        SCOPES
                );
                credential.setSelectedAccount(account.getAccount());

                Classroom service = new Classroom.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        credential)
                        .setApplicationName("HomeworkHelper")
                        .build();

                ListCoursesResponse coursesResponse = service.courses().list()
                        .setStudentId("me")
                        .setCourseStates(Collections.singletonList("ACTIVE"))
                        .execute();

                List<Course> cursos = coursesResponse.getCourses();

                // Limpa a lista antes de adicionar novas tarefas
                mainHandler.post(() -> listaTarefasTexto.clear());

                if (cursos != null) {
                    for (Course curso : cursos) {
                        ListStudentSubmissionsResponse submissionsResponse = service.courses()
                                .courseWork()
                                .studentSubmissions()
                                .list(curso.getId(), "-")
                                .setUserId("me")
                                .execute();

                        List<StudentSubmission> submissoes = submissionsResponse.getStudentSubmissions();

                        if (submissoes != null) {
                            for (StudentSubmission submissao : submissoes) {
                                String estado = submissao.getState();

                                if ("CREATED".equals(estado) || "RECLAIMED_BY_STUDENT".equals(estado)) {

                                    CourseWork tarefaCompleta = service.courses()
                                            .courseWork()
                                            .get(curso.getId(), submissao.getCourseWorkId())
                                            .execute();

                                    String exibirTexto = tarefaCompleta.getTitle() + " (" + curso.getName() + ")";

                                    mainHandler.post(() -> {
                                        listaTarefasTexto.add(exibirTexto);
                                        adapter.notifyDataSetChanged();
                                    });
                                }
                            }
                        }
                    }
                } else {
                    mainHandler.post(() -> Toast.makeText(ClassromActivity.this, "Nenhuma turma ativa encontrada.", Toast.LENGTH_SHORT).show());
                }

            } catch (IOException e) {
                Log.e("ClassroomApp", "Erro ao chamar a API do Classroom", e);
                mainHandler.post(() -> Toast.makeText(ClassromActivity.this, "Erro ao carregar dados do Classroom.", Toast.LENGTH_SHORT).show());
            }
        });
    }
}