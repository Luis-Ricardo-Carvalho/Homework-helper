package br.edu.ifsulminas.mch.homeworkhelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;
import br.edu.ifsulminas.mch.homeworkhelper.model.Task;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.AppDatabase;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.SubjectDao;
import br.edu.ifsulminas.mch.homeworkhelper.model.persistence.TaskDao;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        TaskDao taskDao = AppDatabase.getInstance(context).taskDao();
        SubjectDao subjectDao = AppDatabase.getInstance(context).subjectDao();

        List<Task> tasks = taskDao.listAll();
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Task task : tasks) {
            if (!task.isActive()) continue;
            if (task.getDateSubmission() == null || task.getDateSubmission().isEmpty()) continue;

            try {
                Date deliveryDate = isoFormat.parse(task.getDateSubmission());
                if (deliveryDate == null) continue;

                String subjectName = "";
                for (Subject s : subjectDao.listAll()) {
                    if (s.getId() == task.getSubjectId()) {
                        subjectName = s.getName();
                        break;
                    }
                }

                NotificationHelper.createNotificationChannel(context);

                // Alarme véspera: 1 dia antes às 18:00
                Calendar vesp = Calendar.getInstance();
                vesp.setTime(deliveryDate);
                vesp.add(Calendar.DAY_OF_YEAR, -1);
                vesp.set(Calendar.HOUR_OF_DAY, 18);
                vesp.set(Calendar.MINUTE, 0);
                vesp.set(Calendar.SECOND, 0);
                vesp.set(Calendar.MILLISECOND, 0);

                if (vesp.getTimeInMillis() > System.currentTimeMillis()) {
                    scheduleAlarm(context, task.getId() * 2, task.getName(), subjectName, vesp.getTimeInMillis());
                }

                // Alarme dia da entrega às 06:00
                Calendar diaEntrega = Calendar.getInstance();
                diaEntrega.setTime(deliveryDate);
                diaEntrega.set(Calendar.HOUR_OF_DAY, 6);
                diaEntrega.set(Calendar.MINUTE, 0);
                diaEntrega.set(Calendar.SECOND, 0);
                diaEntrega.set(Calendar.MILLISECOND, 0);

                if (diaEntrega.getTimeInMillis() > System.currentTimeMillis()) {
                    scheduleAlarm(context, task.getId() * 2 + 1, task.getName(), subjectName, diaEntrega.getTimeInMillis());
                }

            } catch (ParseException e) {
                // ignora tarefa com data invalida
            }
        }
    }

    private void scheduleAlarm(Context context, int requestCode, String taskName, String subjectName, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_TASK_ID, requestCode);
        intent.putExtra(NotificationReceiver.EXTRA_TASK_NAME, taskName);
        intent.putExtra(NotificationReceiver.EXTRA_SUBJECT_NAME, subjectName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
