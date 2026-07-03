package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Context;
import android.content.Intent; // Importante para enviar dados para outros apps
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView; // Importante para mapear o botão
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import br.edu.ifsulminas.mch.homeworkhelper.model.Task;

public class TaskAdapter extends ArrayAdapter<Task> {

    private Context context;
    private List<Task> tasks;

    public TaskAdapter(@NonNull Context context, @NonNull List<Task> tasks) {
        super(context, R.layout.item_task, tasks);
        this.context = context;
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        }

        Task task = tasks.get(position);

        TextView txtName = convertView.findViewById(R.id.txt_task_name);
        TextView txtDesc = convertView.findViewById(R.id.txt_task_desc);
        TextView txtDate = convertView.findViewById(R.id.txt_task_date);

        // 1. Encontra o ImageView do compartilhar que adicionamos no XML
        ImageView buttonShare = convertView.findViewById(R.id.imageViewShareItem);

        if (task != null) {
            txtName.setText(task.getName());
            txtDesc.setText(task.getDescription());
            txtDate.setText("Entrega: " + task.getDateSubmission());

            // 2. Configura o clique do botão para enviar os dados desta tarefa específica
            buttonShare.setOnClickListener(v -> {
                compartilharTarefaParaOutrosApps(task);
            });
        }

        return convertView;
    }

    // 3. Método que cria a Intent de compartilhamento do sistema
    private void compartilharTarefaParaOutrosApps(Task task) {
        // Cria uma Intent de envio de texto/dados
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        // Monta o texto formatado com as quebras de linha (\n)
        String textoParaEnviar = "*Tarefa:* " + task.getName() + "\n" +
                "*Descrição:* " + task.getDescription() + "\n" +
                "*Data de Entrega:* " + task.getDateSubmission();

        // Coloca o texto dentro da Intent
        intent.putExtra(Intent.EXTRA_TEXT, textoParaEnviar);

        // Opcional: Adiciona um assunto (útil se o usuário escolher compartilhar por E-mail)
        intent.putExtra(Intent.EXTRA_SUBJECT, "Tarefa: " + task.getName());

        // Abre o menu nativo do Android para o usuário escolher em qual app quer mandar
        context.startActivity(Intent.createChooser(intent, "Enviar tarefa para..."));
    }
}