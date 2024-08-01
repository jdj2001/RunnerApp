package com.example.runnerapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.R;
import com.example.runnerapp.Models.Activity;

import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    private List<Activity> activities;

    public ProgressAdapter(List<Activity> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        Activity activity = activities.get(position);

        holder.dateTextView.setText(activity.getDate());
        holder.detailsTextView.setText(String.format("Distancia: %.2f km\nTiempo: %s\nCalorías: %.2f kcal",
                activity.getDistance(),
                activity.getTime() != null ? activity.getTime() : "No disponible",
                activity.getCaloriesBurned()));

        holder.itemView.setOnClickListener(v -> {
            showActivityDetailDialog(holder.itemView.getContext(), activity);
        });
    }

    private void showActivityDetailDialog(Context context, Activity activity) {
        AlertDialog.Builder detailBuilder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_activity_detail, null);

        TextView dateTextView = dialogView.findViewById(R.id.dialogDateTextView);
        TextView distanceTextView = dialogView.findViewById(R.id.dialogDistanceTextView);
        TextView timeTextView = dialogView.findViewById(R.id.dialogTimeTextView);
        TextView caloriesTextView = dialogView.findViewById(R.id.dialogCaloriesTextView);

        dateTextView.setText(activity.getDate());
        distanceTextView.setText(String.format("Distancia: %.2f km", activity.getDistance()));
        timeTextView.setText("Tiempo: " + (activity.getTime() != null ? activity.getTime() : "No disponible"));
        caloriesTextView.setText(String.format("Calorías: %.2f kcal", activity.getCaloriesBurned()));

        detailBuilder.setView(dialogView)
                .setPositiveButton("Cerrar", (detailDialog, detailWhich) -> detailDialog.dismiss())
                .show();
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView;
        TextView detailsTextView;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            detailsTextView = itemView.findViewById(R.id.detailsTextView);
        }
    }
}





