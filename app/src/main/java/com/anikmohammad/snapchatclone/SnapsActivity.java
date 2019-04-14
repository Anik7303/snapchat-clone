package com.anikmohammad.snapchatclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class SnapsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ListView snapsListView;
    private ArrayList<HashMap<String, String>> snapsList;
    private SimpleAdapter adapter;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;

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
        adapter = new SimpleAdapter(SnapsActivity.this, snapsList, android.R.layout.simple_list_item_2, new String[]{"", ""}, new int[] {, });
        snapsListView.setAdapter(adapter);

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
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
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
