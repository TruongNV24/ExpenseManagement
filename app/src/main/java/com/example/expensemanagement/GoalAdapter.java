package com.example.expensemanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanagement.database.Goal;

import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    public interface OnGoalActionListener {
        void onClick(Goal goal);
    }

    private List<Goal> goals;
    private OnGoalActionListener listener;

    public GoalAdapter(List<Goal> goals, OnGoalActionListener listener) {
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);

        holder.tvGoalName.setText(goal.getName());
        holder.tvGoalAmount.setText(
                String.format("%.2f / %.2f", goal.getCurrentAmount(), goal.getTargetAmount())
        );

        int percent = (int) ((goal.getCurrentAmount() / goal.getTargetAmount()) * 100);
        holder.progressGoal.setProgress(percent);
        holder.tvGoalPercent.setText(percent + "%");

        if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
            holder.tvGoalCompleted.setVisibility(View.VISIBLE);
        } else {
            holder.tvGoalCompleted.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(goal);
        });
    }

    @Override
    public int getItemCount() {
        return goals != null ? goals.size() : 0;
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvGoalAmount, tvGoalPercent, tvGoalCompleted;
        ProgressBar progressGoal;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tvGoalName);
            tvGoalAmount = itemView.findViewById(R.id.tvGoalAmount);
            tvGoalPercent = itemView.findViewById(R.id.tvGoalPercent);
            tvGoalCompleted = itemView.findViewById(R.id.tvGoalCompleted);
            progressGoal = itemView.findViewById(R.id.progressGoal);
        }
    }
}
