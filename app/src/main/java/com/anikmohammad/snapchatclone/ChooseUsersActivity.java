package com.anikmohammad.snapchatclone;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class ChooseUsersActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView usersListView;
    private ArrayList<String> userEmails;
    private ArrayList<String> userUIDs;
    private ArrayAdapter<String> adapter;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_users);
        setTitle("Send To");

        setupVariables();

        populateUsersList();

        usersListView.setOnItemClickListener(ChooseUsersActivity.this);
    }

    private void setupVariables() {
        usersListView = findViewById(R.id.usersListView);
        userEmails = new ArrayList<>();
        userUIDs = new ArrayList<>();
        adapter = new ArrayAdapter<>(ChooseUsersActivity.this, android.R.layout.simple_list_item_1, userEmails);
        usersListView.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.usersListView) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("senderEmail", mAuth.getCurrentUser().getEmail());
            hashMap.put("imageName", getIntent().getStringExtra("imageName"));
            hashMap.put("imageUrl", getIntent().getStringExtra("imageUrl"));
            hashMap.put("message", getIntent().getStringExtra("message"));
            FirebaseDatabase.getInstance().getReference().child("users").child(userUIDs.get(position)).child("snaps").push().setValue(hashMap);
            startActivity(new Intent(ChooseUsersActivity.this, SnapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    private void populateUsersList() {
        FirebaseDatabase.getInstance().getReference().child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String email = dataSnapshot.child("email").getValue().toString();
                Log.i("Values", String.format("email: %s", email));
                userEmails.add(email);
                userUIDs.add(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        adapter.notifyDataSetChanged();
    }

    private void handleException(Exception e, String title) {
        Log.i(String.format("Error - %s", title), e.getMessage());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
