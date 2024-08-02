package com.example.runnerapp.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.Adapters.FriendsListAdapter;
import com.example.runnerapp.Models.User;
import com.example.runnerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView friendsRecyclerView;
    private FriendsListAdapter friendsListAdapter;
    private List<User> friendsList;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private String currentUserId;
    private TextView noFriendsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        noFriendsTextView = findViewById(R.id.noFriendsTextView);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        friendsList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users");

            loadFriendsList();
        }

        friendsListAdapter = new FriendsListAdapter(friendsList, new FriendsListAdapter.OnItemClickListener() {
            @Override
            public void onViewProfileClick(User user) {
                Intent intent = new Intent(FriendsListActivity.this, PerfilActivity.class);
                intent.putExtra("userId", user.getUserId());
                startActivity(intent);
            }

            @Override
            public void onDeleteFriendClick(User user) {
                deleteFriend(user);
            }
        });

        friendsRecyclerView.setAdapter(friendsListAdapter);
    }

    private void toggleNoRequestsMessage() {
        if (friendsList.isEmpty()) {
            noFriendsTextView.setVisibility(View.VISIBLE);
            friendsRecyclerView.setVisibility(View.GONE);
        } else {
            noFriendsTextView.setVisibility(View.GONE);
            friendsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadFriendsList() {
        DatabaseReference friendsRef = usersRef.child(currentUserId).child("friends");
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String friendId = snapshot.getKey();
                    usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User friend = dataSnapshot.getValue(User.class);
                            if (friend != null) {
                                friendsList.add(friend);
                                friendsListAdapter.notifyDataSetChanged();
                            }
                            toggleNoRequestsMessage();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("FriendsListActivity", "Error loading friend data: " + databaseError.getMessage());
                        }
                    });
                }
                toggleNoRequestsMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FriendsListActivity", "Error loading friends list: " + databaseError.getMessage());
            }
        });
    }

    private void deleteFriend(User friend) {
        DatabaseReference currentUserRef = usersRef.child(currentUserId).child("friends").child(friend.getUserId());
        DatabaseReference friendRef = usersRef.child(friend.getUserId()).child("friends").child(currentUserId);

        currentUserRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                friendRef.removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        friendsList.remove(friend);
                        friendsListAdapter.notifyDataSetChanged();
                        Toast.makeText(FriendsListActivity.this, "Amigo eliminado", Toast.LENGTH_SHORT).show();
                        loadFriendsList(); // Actualiza la lista de amigos
                    } else {
                        Toast.makeText(FriendsListActivity.this, "Error al eliminar amigo", Toast.LENGTH_SHORT).show();
                    }
                });
                toggleNoRequestsMessage();
            } else {
                Toast.makeText(FriendsListActivity.this, "Error al eliminar amigo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



