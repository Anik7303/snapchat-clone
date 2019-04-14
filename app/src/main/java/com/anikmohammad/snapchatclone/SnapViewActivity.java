package com.anikmohammad.snapchatclone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SnapViewActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private String imageUrl;
    private String message;
    private String imageName;
    private String snapId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_view);
        setTitle(String.format("From: %s", getIntent().getStringExtra("senderEmail")));

        setupVariables();
    }

    private void setupVariables() {
        imageView = findViewById(R.id.showSnapImageView);
        textView = findViewById(R.id.messageTextView);
        imageName = getIntent().getStringExtra("imageName");
        imageUrl = getIntent().getStringExtra("imageUrl");
        message = getIntent().getStringExtra("message");
        snapId = getIntent().getStringExtra("snapId");
        Log.i("ImageUrl", imageUrl);
        Log.i("message", message);
        try {
            ImageDownloader task = new ImageDownloader();
            task.execute(imageUrl);
        }catch(Exception e) {
            handleException(e, "setupVariables");
        }
        textView.setText(message);
    }

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(input);
                return image;
            }catch(Exception e) {
                handleException(e, "doInBackground");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            try {
                imageView.setImageBitmap(bitmap);
            }catch(Exception e) {
                handleException(e, "onPostExecute");
            }
        }
    }

    private void deleteSnap() {
        FirebaseStorage.getInstance().getReference().child("images")
                .child(imageName)
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, "deleteSnap: Image");
                    }
                });
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("snaps")
                .child(snapId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(SnapViewActivity.this, "Snap Deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            handleException(task.getException(), "deleteSnap: Snap");
                        }
                    }
                });
    }

    private void handleException(Exception e, String title) {
        Log.i(String.format("Error - %s", title), e.getMessage());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteSnap();
    }
}
