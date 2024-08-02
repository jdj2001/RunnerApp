package com.example.runnerapp.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runnerapp.Adapters.FriendsAdapter;
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

public class FriendsFragment extends Fragment {

    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private List<User> friendsList;
    private List<User> filteredList;
    private DatabaseReference usersRef;
    private EditText searchEditText;
    private FirebaseUser currentUser;
    private String currentUserId;
    private String currentUserCountry;
    private Button pendingRequestsButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        searchEditText = view.findViewById(R.id.searchEditText);
        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingRequestsButton = view.findViewById(R.id.pendingRequestsButton);

        friendsList = new ArrayList<>();
        filteredList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users");

            DatabaseReference currentUserRef = usersRef.child(currentUserId);
            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        currentUserCountry = user.getCountry();
                        Log.d("FriendsFragment", "Current user country: " + currentUserCountry);
                        loadFriends();
                    } else {
                        Log.e("FriendsFragment", "User is null.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FriendsFragment", "Error loading current user data: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("FriendsFragment", "Current user is null.");
        }

        friendsAdapter = new FriendsAdapter(filteredList, new FriendsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                showFriendOptionsDialog(user);
            }
        });
        friendsRecyclerView.setAdapter(friendsAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        pendingRequestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PendingRequestsActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadFriends() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        Log.d("FriendsFragment", "User found: " + user.getUserId() + ", Country: " + user.getCountry());
                        if (user.getCountry() != null
                                && user.getCountry().equals(currentUserCountry) && !user.getUserId().equals(currentUserId)) {
                            friendsList.add(user);
                        }
                    }
                }
                filterFriends(searchEditText.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FriendsFragment", "Error loading user data: " + databaseError.getMessage());
            }
        });
    }

    private void filterFriends(String query) {
        filteredList.clear();
        for (User user : friendsList) {
            if (user.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getLastName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        friendsAdapter.notifyDataSetChanged();
    }

    private void showFriendOptionsDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(user.getFirstName() + " " + user.getLastName());
        builder.setItems(new CharSequence[]{"Agregar amigo", "Ver perfil"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    sendFriendRequest(user);
                    break;
                case 1:
                    viewProfile(user);
                    break;
            }
        });
        builder.show();
    }

    private void sendFriendRequest(User user) {
        DatabaseReference friendRequestRef = usersRef.child(user.getUserId()).child("friendRequests").child(currentUserId);
        friendRequestRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Solicitud de amistad enviada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error al enviar solicitud de amistad", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void viewProfile(User user) {
        Intent intent = new Intent(requireContext(), PerfilActivity.class);
        intent.putExtra("userId", user.getUserId());
        startActivity(intent);
    }
}




