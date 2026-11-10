package com.example.studenttaskmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that renders task cards.
 * Tapping a card toggles its status; long-pressing opens the manage menu.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onTaskToggleRequested(Task task);

        void onTaskMenuRequested(Task task);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final OnTaskActionListener listener;

    public TaskAdapter(OnTaskActionListener listener) {
        this.listener = listener;
    }

    /** Replaces the currently displayed tasks and refreshes the list. */
    public void setTasks(List<Task> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        final Task task = tasks.get(position);
        final boolean completed = task.getCompleted() == 1;
        final boolean overdue = task.isOverdue();

        holder.txtTitle.setText(task.getTitle());
        holder.txtDescription.setText(
                task.getDescription().isEmpty()
                        ? holder.itemView.getContext().getString(R.string.no_description)
                        : task.getDescription());

        // Due date line
        String dueText = holder.itemView.getContext()
                .getString(R.string.due_prefix, task.getFormattedDueDate());
        holder.txtDueDate.setText(dueText);

        if (overdue) {
            holder.txtDueDate.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.overdue_text));
        } else {
            holder.txtDueDate.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.on_muted_fg));
        }

        // Status chip
        if (completed) {
            holder.txtStatus.setText(R.string.status_completed);
            holder.txtStatus.setBackgroundResource(R.drawable.chip_bg_complete);
            holder.txtStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.complete_text));
            // Status icon: bright enough to survive the black background
        holder.imgStatus.setImageResource(
                completed ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);
        holder.imgStatus.setColorFilter(ContextCompat.getColor(
                holder.itemView.getContext(),
                completed ? R.color.on_surface : R.color.on_muted_fg));
            holder.txtDueDate.setAlpha(0.6f);
        } else if (overdue) {
            holder.txtStatus.setText(R.string.status_overdue);
            holder.txtStatus.setBackgroundResource(R.drawable.chip_bg_overdue);
            holder.txtStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.overdue_text));
            holder.txtDueDate.setAlpha(1f);
        } else {
            holder.txtStatus.setText(R.string.status_pending);
            holder.txtStatus.setBackgroundResource(R.drawable.chip_bg_pending);
            holder.txtStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.pending_text));
            holder.txtDueDate.setAlpha(1f);
        }

        // Interaction
        holder.itemView.setOnClickListener(v -> listener.onTaskToggleRequested(task));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onTaskMenuRequested(task);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        final TextView txtTitle;
        final TextView txtDescription;
        final TextView txtDueDate;
        final TextView txtStatus;
        final ImageView imgStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDueDate = itemView.findViewById(R.id.txtDueDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }
}
