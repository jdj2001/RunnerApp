package com.example.runnerapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runnerapp.Models.User;
import com.example.runnerapp.R;

import java.util.List;

public class PendingRequestsAdapter extends RecyclerView.Adapter<PendingRequestsAdapter.PendingRequestViewHolder> {

    private List<User> pendingRequestsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAcceptClick(User user);
        void onRejectClick(User user);
    }

    public PendingRequestsAdapter(List<User> pendingRequestsList, OnItemClickListener listener) {
        this.pendingRequestsList = pendingRequestsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PendingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_request, parent, false);
        return new PendingRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingRequestViewHolder holder, int position) {
        User requester = pendingRequestsList.get(position);
        holder.nameTextView.setText(requester.getFirstName() + " " + requester.getLastName());
        holder.countryTextView.setText(requester.getCountry());
        Glide.with(holder.itemView.getContext())
                .load(requester.getProfileImageUrl())
                .placeholder(R.drawable.ic_profile)
                .into(holder.profileImageView);

        holder.acceptButton.setOnClickListener(v -> listener.onAcceptClick(requester));
        holder.rejectButton.setOnClickListener(v -> listener.onRejectClick(requester));
    }

    @Override
    public int getItemCount() {
        return pendingRequestsList.size();
    }

    static class PendingRequestViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView countryTextView;
        ImageView profileImageView;
        Button acceptButton;
        Button rejectButton;

        public PendingRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.requesterName);
            countryTextView = itemView.findViewById(R.id.requesterCountry);
            profileImageView = itemView.findViewById(R.id.requesterProfileImageView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}
