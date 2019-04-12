package com.anikmohammad.snapchatclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class CreateSnapActivity extends AppCompatActivity {

    private ImageView snapImageView;
    private EditText messageEditText;
    private Button uploadButton;
    private Button sendButton;

    private int imageRequestCode;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_snap);
        setTitle("Send Snap");

        setupVariables();
    }

    private void setupVariables() {
        snapImageView = findViewById(R.id.snapImageView);
        messageEditText = findViewById(R.id.messageEditText);
        uploadButton = findViewById(R.id.uploadButton);
        sendButton = findViewById(R.id.sendButton);
        imageRequestCode = 1;
    }

    protected void uploadImage(View view) {
        if(ContextCompat.checkSelfPermission(CreateSnapActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getImage();
        }else {
            ActivityCompat.requestPermissions(CreateSnapActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, imageRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == imageRequestCode && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == imageRequestCode && resultCode == RESULT_OK && data != null) {
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(CreateSnapActivity.this.getContentResolver(), data.getData());
                snapImageView.setImageBitmap(imageBitmap);
            }catch(Exception e) {
                handleException(e, "OnActivityResult");
            }
        }
    }

    private void getImage() {
         Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         startActivityForResult(intent, imageRequestCode);
    }

    protected void sendSnap(View view) {
        String message = messageEditText.getText().toString();

        snapImageView.setDrawingCacheEnabled(true);
        snapImageView.buildDrawingCache();
        Bitmap bitmapImage = ((BitmapDrawable) snapImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] array = outputStream.toByteArray();

        storageReference = FirebaseStorage.getInstance()
                .getReference()
                .child("images")
                .child(UUID.randomUUID()+".jpeg");
        UploadTask uploadTask = storageReference.putBytes(array);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri uri =  task.getResult();
                Log.i("Download url", uri.toString());
            }
        });

//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(CreateSnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Log.i("Image URL", taskSnapshot.getUploadSessionUri().toString());
//                Log.i("Image URL storage", taskSnapshot.getStorage().getDownloadUrl().toString());
//                Log.i("Image URL task", taskSnapshot.getTask().getResult().toString());
//                Log.i("Image URL reference", storageReference.getDownloadUrl().toString());
//            }
//        });
    }

    private void handleException(Exception e, String title) {
        Toast.makeText(CreateSnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.i(String.format("Error - %s", title), e.getMessage());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
