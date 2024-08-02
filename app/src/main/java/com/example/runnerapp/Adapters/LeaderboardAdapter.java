package com.example.runnerapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.runnerapp.R;
import com.example.runnerapp.Models.User;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<User> leaderboardUsers;

    public LeaderboardAdapter(List<User> leaderboardUsers) {
        this.leaderboardUsers = leaderboardUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = leaderboardUsers.get(position);
        holder.bind(user, position + 1);
    }

    @Override
    public int getItemCount() {
        return leaderboardUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView positionTextView;
        private TextView usernameTextView;
        private TextView distanceTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.positionTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
        }

        public void bind(User user, int position) {
            positionTextView.setText(String.valueOf(position));
            usernameTextView.setText(user.getFirstName() + " " + user.getLastName());
            // Formatear la distancia con dos dígitos después del punto decimal
            String formattedDistance = String.format("%.2f", user.getDistanceTraveled());
            distanceTextView.setText(formattedDistance + " km");
        }
    }
}

