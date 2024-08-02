package com.example.runnerapp.Tabs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.Adapters.PendingRequestsAdapter;
import com.example.runnerapp.Models.FirebaseOperations;
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

public class PendingRequestsActivity extends AppCompatActivity {

    private RecyclerView pendingRequestsRecyclerView;
    private PendingRequestsAdapter pendingRequestsAdapter;
    private List<User> pendingRequestsList;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private String currentUserId;
    private TextView noRequestsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_requests);

        noRequestsTextView = findViewById(R.id.noRequestsTextView);
        pendingRequestsRecyclerView = findViewById(R.id.pendingRequestsRecyclerView);
        pendingRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        pendingRequestsList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users");

            loadPendingRequests();
        } else {
            Log.e("PendingRequestsActivity", "Current user is null.");
        }

        pendingRequestsAdapter = new PendingRequestsAdapter(pendingRequestsList, new PendingRequestsAdapter.OnItemClickListener() {
            @Override
            public void onAcceptClick(User user) {
                acceptFriendRequest(user);
            }

            @Override
            public void onRejectClick(User user) {
                rejectFriendRequest(user);
            }
        });
        pendingRequestsRecyclerView.setAdapter(pendingRequestsAdapter);
    }

    private void loadPendingRequests() {
        DatabaseReference pendingRequestsRef = usersRef.child(currentUserId).child("friendRequests");
        pendingRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingRequestsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String requesterId = snapshot.getKey();
                    usersRef.child(requesterId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                pendingRequestsList.add(user);
                                pendingRequestsAdapter.notifyDataSetChanged();
                            }
                            toggleNoRequestsMessage();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("PendingRequestsActivity", "Error loading user data: " + databaseError.getMessage());
                        }
                    });
                }
                toggleNoRequestsMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PendingRequestsActivity", "Error loading pending requests: " + databaseError.getMessage());
            }
        });
    }

    private void toggleNoRequestsMessage() {
        if (pendingRequestsList.isEmpty()) {
            noRequestsTextView.setVisibility(View.VISIBLE);
            pendingRequestsRecyclerView.setVisibility(View.GONE);
        } else {
            noRequestsTextView.setVisibility(View.GONE);
            pendingRequestsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void acceptFriendRequest(User user) {
        FirebaseOperations.addFriend(currentUserId, user.getUserId(), (success, message) -> {
            if (success) {
                removePendingRequest(user);
                Toast.makeText(PendingRequestsActivity.this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PendingRequestsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectFriendRequest(User user) {
        DatabaseReference pendingRequestsRef = usersRef.child(currentUserId).child("friendRequests").child(user.getUserId());
        pendingRequestsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pendingRequestsList.remove(user);
                pendingRequestsAdapter.notifyDataSetChanged();
                toggleNoRequestsMessage();
                Toast.makeText(PendingRequestsActivity.this, "Solicitud de amistad rechazada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PendingRequestsActivity.this, "Error al rechazar la solicitud de amistad", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removePendingRequest(User user) {
        DatabaseReference pendingRequestsRef = usersRef.child(currentUserId).child("friendRequests").child(user.getUserId());
        pendingRequestsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pendingRequestsList.remove(user);
                pendingRequestsAdapter.notifyDataSetChanged();
                toggleNoRequestsMessage();
            }
        });
    }
}
