package com.example.runnerapp.Pestañas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runnerapp.Models.User;
import com.example.runnerapp.R;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<User> friendsList;

    public FriendsAdapter(List<User> friendsList) {
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendsList.get(position);
        holder.nameTextView.setText(friend.getFirstName() + " " + friend.getLastName());
        holder.countryTextView.setText(friend.getCountry());
        // Cargar la imagen de perfil usando Glide
        Glide.with(holder.itemView.getContext())
                .load(friend.getProfileImageUrl())
                .placeholder(R.drawable.ic_profile)
                .into(holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView countryTextView;
        ImageView profileImageView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.friendName);
            countryTextView = itemView.findViewById(R.id.friendCountry);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
}
