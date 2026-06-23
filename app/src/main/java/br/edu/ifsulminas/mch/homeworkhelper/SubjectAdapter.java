package br.edu.ifsulminas.mch.homeworkhelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        Subject subject = subjects.get(position);
        holder.bind(subject, listener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtDetails;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtSubjectName);
            txtDetails = itemView.findViewById(R.id.txtSubjectDetails);
        }

        public void bind(Subject subject, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
            txtName.setText(subject.getName());
            txtDetails.setText(subject.getTeacher() + " • " + subject.getSchoolYear());

            itemView.setOnClickListener(v -> listener.onItemClick(subject));

            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(subject);
                return true;
            });
        }
    }
}