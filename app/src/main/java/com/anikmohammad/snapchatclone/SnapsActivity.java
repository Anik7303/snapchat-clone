package com.anikmohammad.snapchatclone;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class SnapsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseAuth mAuth;
    private ListView snapsListView;
    private ArrayList<HashMap<String, String>> snapsList;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snaps);
        setTitle("Snaps");

        setupVariables();
    }

    private void setupVariables() {
        mAuth = FirebaseAuth.getInstance();
        snapsListView = findViewById(R.id.snapsListView);
        snapsList = new ArrayList<>();
        adapter = new SimpleAdapter(SnapsActivity.this, snapsList, android.R.layout.simple_list_item_2, new String[]{"message", "senderEmail"}, new int[] {android.R.id.text1, android.R.id.text2});
        snapsListView.setAdapter(adapter);
        snapsListView.setOnItemClickListener(SnapsActivity.this);
        populateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(SnapsActivity.this);
        inflater.inflate(R.menu.snaps_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case R.id.create_snap:
                redirectToCreateSnap();
                break;
            case R.id.logout:
                mAuth.signOut();
                finish();
                break;
            default:
                return false;
        }
        return true;
    }

    private void populateListView() {
        snapsList.clear();
        Log.i("List view population", "running");
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        final String userId = dataSnapshot.getKey();
                        Log.i("currentUserID", FirebaseAuth.getInstance().getUid());
                        if(dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users")
                                    .child(userId)
                                    .child("snaps")
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                            final String snapId = dataSnapshot.getKey();
                                            HashMap<String, String> temp = new HashMap<>();
                                            temp.put("senderEmail", dataSnapshot.child("senderEmail").getValue().toString());
                                            temp.put("message", dataSnapshot.child("message").getValue().toString());
                                            temp.put("imageUrl", dataSnapshot.child("imageUrl").getValue().toString());
                                            temp.put("snapId", dataSnapshot.getKey());
                                            snapsList.add(temp);
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                        }
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.snapsListView) {
            Intent intent = new Intent(SnapsActivity.this, SnapViewActivity.class);
            HashMap<String, String> temp = snapsList.get(position);
            intent.putExtra("senderUid", temp.get("senderId"));
            intent.putExtra("senderEmail", temp.get("senderEmail"));
            intent.putExtra("imageUrl", temp.get("imageUrl"));
            intent.putExtra("message", temp.get("message"));
            startActivity(intent);
        }
    }

    private void redirectToCreateSnap() {
        startActivity(new Intent(SnapsActivity.this, CreateSnapActivity.class));
    }

    private void handleException(Exception e, String title) {
        Toast.makeText(SnapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.i(String.format("Error - %s", title), e.getMessage());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAuth.signOut();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
