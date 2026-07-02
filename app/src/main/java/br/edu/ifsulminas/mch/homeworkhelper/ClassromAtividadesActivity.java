package br.edu.ifsulminas.mch.homeworkhelper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.CourseWork;
import com.google.api.services.classroom.model.ListCourseWorkResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassromAtividadesActivity extends AppCompatActivity {

    private RecyclerView rvAtividades;
    private ArrayList<AtividadeModel> listaAtividades;
    private AssignmentsAdapter adapter;

    private String courseId;
    private String courseName;

    // Escopo necessário para ler as atividades do curso
    private static final List<String> SCOPES = Collections.singletonList(
            ClassroomScopes.CLASSROOM_COURSEWORK_ME_READONLY
    );

    // --- MODELO INTERNO PARA CAPTURAR DADOS DA ATIVIDADE ---
    public static class AtividadeModel {
        private final String titulo;
        private final String descricao;

        public AtividadeModel(String titulo, String descricao) {
            this.titulo = titulo;
            this.descricao = (descricao != null && !descricao.isEmpty()) ? descricao : "Sem descrição.";
        }

        public String getTitulo() { return titulo; }
        public String getDescricao() { return descricao; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_atividades);

        // Captura os dados passados pela ClassroomListActivity
        courseId = getIntent().getStringExtra("COURSE_ID");
        courseName = getIntent().getStringExtra("COURSE_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar_assignments);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && courseName != null) {
            getSupportActionBar().setTitle(courseName); // Coloca o nome da matéria no topo
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rvAtividades = findViewById(R.id.assignments_list);
        rvAtividades.setLayoutManager(new LinearLayoutManager(this));

        listaAtividades = new ArrayList<>();
        adapter = new AssignmentsAdapter(listaAtividades);
        rvAtividades.setAdapter(adapter);

        // Verifica se o usuário está logado e busca as tarefas
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && courseId != null) {
            buscarAtividadesDoCurso(account);
        } else {
            Toast.makeText(this, "Erro ao identificar login ou turma.", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarAtividadesDoCurso(GoogleSignInAccount account) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        ClassromAtividadesActivity.this,
                        SCOPES
                );
                credential.setSelectedAccount(account.getAccount());

                Classroom service = new Classroom.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        credential)
                        .setApplicationName("HomeworkHelper")
                        .build();

                // Busca a lista de atividades específicas daquele ID de curso
                ListCourseWorkResponse courseworkResponse = service.courses().courseWork().list(courseId)
                        .execute();

                List<CourseWork> tarefas = courseworkResponse.getCourseWork();

                mainHandler.post(() -> listaAtividades.clear());

                if (tarefas != null && !tarefas.isEmpty()) {
                    for (CourseWork tarefa : tarefas) {
                        AtividadeModel novaAtiv = new AtividadeModel(tarefa.getTitle(), tarefa.getDescription());

                        mainHandler.post(() -> {
                            listaAtividades.add(novaAtiv);
                            adapter.notifyDataSetChanged();
                        });
                    }
                } else {
                    mainHandler.post(() -> Toast.makeText(ClassromAtividadesActivity.this, "Nenhuma atividade encontrada nesta matéria.", Toast.LENGTH_SHORT).show());
                }

            } catch (IOException e) {
                Log.e("ClassroomApp", "Erro ao buscar atividades", e);
                mainHandler.post(() -> Toast.makeText(ClassromAtividadesActivity.this, "Erro ao carregar atividades.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // --- ADAPTADOR --- Reutiliza o 'item_subject.xml' alterando os dados para Atividades
    private static class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentsAdapter.ViewHolderAtividade> {
        private final List<AtividadeModel> dados;

        public AssignmentsAdapter(List<AtividadeModel> dados) {
            this.dados = dados;
        }

        @NonNull
        @Override
        public ViewHolderAtividade onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subject, parent, false);
            return new ViewHolderAtividade(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderAtividade holder, int position) {
            AtividadeModel atividade = dados.get(position);
            holder.txtTitulo.setText(atividade.getTitulo());
            holder.txtDescricao.setText(atividade.getDescricao());
        }

        @Override
        public int getItemCount() {
            return dados.size();
        }

        public static class ViewHolderAtividade extends RecyclerView.ViewHolder {
            android.widget.TextView txtTitulo;
            android.widget.TextView txtDescricao;

            public ViewHolderAtividade(@NonNull android.view.View itemView) {
                super(itemView);
                txtTitulo = itemView.findViewById(R.id.txtSubjectName);
                txtDescricao = itemView.findViewById(R.id.txtSubjectDetails);
            }
        }
    }
}