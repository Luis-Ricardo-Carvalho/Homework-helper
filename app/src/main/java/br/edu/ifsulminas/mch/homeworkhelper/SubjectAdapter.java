package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import br.edu.ifsulminas.mch.homeworkhelper.model.Subject;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final List<Subject> subjects;
    private final OnItemClickListener listener;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Subject subject);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Subject subject);
    }

    public SubjectAdapter(List<Subject> subjects, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.subjects = subjects;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        holder.bind(subjects.get(position), listener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView txtName;
        TextView txtDetails;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView   = itemView.findViewById(R.id.subject_card);
            txtName    = itemView.findViewById(R.id.txtSubjectName);
            txtDetails = itemView.findViewById(R.id.txtSubjectDetails);
        }

        public void bind(Subject subject, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
            txtName.setText(subject.getName());
            txtDetails.setText(subject.getTeacher() + " • " + subject.getSchoolYear());

            // Aplica a cor do card
            Context ctx = itemView.getContext();
            String colorKey = subject.getColor();
            if (colorKey != null && !colorKey.isEmpty()) {
                int colorResId = ctx.getResources().getIdentifier(colorKey, "color", ctx.getPackageName());
                if (colorResId != 0) {
                    cardView.setCardBackgroundColor(ctx.getColor(colorResId));
                }
            }

            itemView.setOnClickListener(v -> listener.onItemClick(subject));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(subject);
                return true;
            });
        }
    }
}