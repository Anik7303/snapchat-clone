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
    private boolean sendButtonClicked;
    private String imageName;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_snap);
        setTitle("Create");

        setupVariables();
    }

    private void setupVariables() {
        sendButtonClicked = false;
        snapImageView = findViewById(R.id.snapImageView);
        messageEditText = findViewById(R.id.messageEditText);
        uploadButton = findViewById(R.id.uploadButton);
        sendButton = findViewById(R.id.sendButton);
        imageRequestCode = 1;
        storageReference = FirebaseStorage.getInstance().getReference();
        imageName = String.format("%s.jpeg", UUID.randomUUID().toString());
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
        try {
            sendButton.setEnabled(false);
            snapImageView.setDrawingCacheEnabled(true);
            snapImageView.buildDrawingCache();
            Bitmap bitmapImage = ((BitmapDrawable) snapImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] array = outputStream.toByteArray();

            storageReference = FirebaseStorage.getInstance()
                    .getReference()
                    .child("images")
                    .child(imageName);

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
                    try {
                        sendButtonClicked = true;
                        Uri uri = task.getResult();
                        Log.i("Download url", uri.toString());
                        Intent intent = new Intent(CreateSnapActivity.this, ChooseUsersActivity.class);
                        intent.putExtra("imageUrl", uri.toString());
                        intent.putExtra("imageName", imageName);
                        intent.putExtra("message", messageEditText.getText().toString());
                        startActivity(intent);
                    } catch (Exception e) {
                        handleException(e, "Upload Image");
                        sendButton.setEnabled(true);
                    }
                }
            });
        }catch(Exception e) {
            handleException(e, "sendSnap");
            sendButton.setEnabled(true);
        }
    }

    private void deleteImage() {
        FirebaseStorage.getInstance().getReference().child("images")
                .child(imageName)
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, "deleteImage");
                    }
                });
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sendButtonClicked) {
            deleteImage();
            sendButtonClicked = false;
            sendButton.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
