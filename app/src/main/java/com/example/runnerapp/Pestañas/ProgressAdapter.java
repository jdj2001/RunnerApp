package com.example.runnerapp.Pestañas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.Activity;
import com.example.runnerapp.R;

import java.util.List;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    private List<Activity> activityList;

    public ProgressAdapter(List<Activity> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        holder.dateTextView.setText(activity.getDate());
        holder.distanceTextView.setText("Distancia: " + activity.getDistance() + " km");
        holder.timeTextView.setText("Tiempo: " + activity.getTime() + " min");
        holder.routeTextView.setText("Ruta: " + activity.getRoute());
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView;
        TextView distanceTextView;
        TextView timeTextView;
        TextView routeTextView;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            routeTextView = itemView.findViewById(R.id.routeTextView);
        }
    }
}
