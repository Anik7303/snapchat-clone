package com.anikmohammad.snapchatclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.TtsSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SnapViewActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private String imageUrl;
    private String message;

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
        imageUrl = getIntent().getStringExtra("imageUrl");
        message = getIntent().getStringExtra("message");
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

    private void handleException(Exception e, String title) {
        Log.i(String.format("Error - %s", title), e.getMessage());
    }
}
