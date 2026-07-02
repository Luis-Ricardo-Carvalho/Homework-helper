package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.ListCoursesResponse;
import com.google.api.services.classroom.model.UserProfile;

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

    private RecyclerView rvCursos;
    private ArrayList<Turma> listaTurmas;
    private ClassroomAdapter adapter;

    private static final List<String> SCOPES = Arrays.asList(
            ClassroomScopes.CLASSROOM_COURSES_READONLY,
            ClassroomScopes.CLASSROOM_ROSTERS_READONLY,
            "profile"
    );

    // --- CLASSE MODELO INTERNA ATUALIZADA ---
    public static class Turma {
        private final String id; // ADICIONADO: Guarda o ID único do Google Classroom
        private final String nome;
        private final String professor;

        public Turma(String id, String nome, String professor) {
            this.id = id;
            this.nome = nome;
            this.professor = professor;
        }

        // Métodos Getters
        public String getId() { return id; }
        public String getNome() { return nome; }
        public String getProfessor() { return professor; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Button btnLogin = findViewById(R.id.btn_login);
        FloatingActionButton fab = findViewById(R.id.fab);

        rvCursos = findViewById(R.id.subject_list);
        rvCursos.setLayoutManager(new LinearLayoutManager(this));

        listaTurmas = new ArrayList<>();
        adapter = new ClassroomAdapter(listaTurmas);
        rvCursos.setAdapter(adapter);

        fab.setOnClickListener(v -> {
            Toast.makeText(this, "Ação do FAB!", Toast.LENGTH_SHORT).show();
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                        new Scope(ClassroomScopes.CLASSROOM_COURSES_READONLY),
                        new Scope(ClassroomScopes.CLASSROOM_ROSTERS_READONLY),
                        new Scope("profile")
                )
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

                mainHandler.post(() -> listaTurmas.clear());

                if (cursos != null && !cursos.isEmpty()) {
                    for (Course curso : cursos) {
                        String idTurma = curso.getId(); // ADICIONADO: Pega o ID da Turma
                        String nomeTurma = curso.getName();
                        String nomeProfessor = "Professor não identificado";

                        try {
                            UserProfile perfil = service.userProfiles().get(curso.getOwnerId()).execute();
                            if (perfil != null && perfil.getName() != null) {
                                nomeProfessor = perfil.getName().getFullName();
                            }
                        } catch (IOException e) {
                            Log.e("ClassroomApp", "Não foi possível carregar o nome do professor para: " + nomeTurma, e);
                        }

                        // MODIFICADO: Passando o idTurma para o nosso construtor da classe modelo
                        Turma novaTurma = new Turma(idTurma, nomeTurma, nomeProfessor);

                        mainHandler.post(() -> {
                            listaTurmas.add(novaTurma);
                            adapter.notifyDataSetChanged();
                        });
                    }
                } else {
                    mainHandler.post(() -> Toast.makeText(ClassromActivity.this, "Nenhuma turma ativa encontrada.", Toast.LENGTH_SHORT).show());
                }

            } catch (IOException e) {
                Log.e("ClassroomApp", "Erro ao chamar a API do Classroom", e);
                mainHandler.post(() -> Toast.makeText(ClassromActivity.this, "Erro ao carregar turmas.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // --- ADAPTADOR DO RECYCLERVIEW ---
    private static class ClassroomAdapter extends RecyclerView.Adapter<ClassroomAdapter.ViewHolderTurma> {
        private final List<Turma> dados;

        public ClassroomAdapter(List<Turma> dados) {
            this.dados = dados;
        }

        @NonNull
        @Override
        public ViewHolderTurma onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subject, parent, false);
            return new ViewHolderTurma(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderTurma holder, int position) {
            Turma turmaAtual = dados.get(position);
            holder.txtSubjectName.setText(turmaAtual.getNome());
            holder.txtSubjectDetails.setText(turmaAtual.getProfessor());

            // ADICIONADO: Configura o clique no card de cada matéria
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ClassromAtividadesActivity.class);
                // Enviamos o ID e o Nome coletados para a tela de Atividades
                intent.putExtra("COURSE_ID", turmaAtual.getId());
                intent.putExtra("COURSE_NAME", turmaAtual.getNome());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return dados.size();
        }

        public static class ViewHolderTurma extends RecyclerView.ViewHolder {
            android.widget.TextView txtSubjectName;
            android.widget.TextView txtSubjectDetails;

            public ViewHolderTurma(@NonNull android.view.View itemView) {
                super(itemView);
                txtSubjectName = itemView.findViewById(R.id.txtSubjectName);
                txtSubjectDetails = itemView.findViewById(R.id.txtSubjectDetails);
            }
        }
    }
}