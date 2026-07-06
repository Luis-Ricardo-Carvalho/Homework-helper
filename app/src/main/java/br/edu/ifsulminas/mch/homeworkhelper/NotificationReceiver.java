package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_NAME = "extra_task_name";
    public static final String EXTRA_SUBJECT_NAME = "extra_subject_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(EXTRA_TASK_ID, 0);
        String taskName = intent.getStringExtra(EXTRA_TASK_NAME);
        String subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME);

        if (taskName == null) taskName = "Tarefa";
        if (subjectName == null) subjectName = "";

        String title = "Lembrete de entrega";
        String message = "A tarefa \"" + taskName + "\"" +
                (subjectName.isEmpty() ? "" : " de " + subjectName) +
                " esta proxima do prazo!";

        NotificationHelper.showNotification(context, taskId, title, message);
    }
}
