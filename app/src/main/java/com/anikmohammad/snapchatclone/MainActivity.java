package com.anikmohammad.snapchatclone;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button actionButton;
    private TextView alternateTextView;
    private TextView alternatePrefixTextView;

    private boolean loginState;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Snapchat Clone");

        setupVariables();

        if(mAuth.getCurrentUser() != null) {
            redirectToSnaps();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.alternateTextView) {
            alternateState();
        }
    }

    private void setupVariables() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        actionButton = findViewById(R.id.actionButton);
        alternateTextView = findViewById(R.id.alternateTextView);
        alternatePrefixTextView = findViewById(R.id.alternatePrefixTextView);
        emailEditText.setText("");
        passwordEditText.setText("");
        setLoginState();
        mAuth = FirebaseAuth.getInstance();
        alternateTextView.setOnClickListener(MainActivity.this);
    }

    protected void actionFunction(View view) {
        final String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        actionButton.setEnabled(false);

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill email address and password correctly", Toast.LENGTH_SHORT).show();
            return;
        }
        if(loginState) {
            Log.i("Action", "login");
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                redirectToSnaps();
                            }else {
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                actionButton.setEnabled(true);
                            }
                        }
                    });
        }else {
            Log.i("Action", "sign up");
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                // add to database
                                FirebaseDatabase.getInstance().getReference().child("users").child(task.getResult().getUser().getUid()).child("email").setValue(email);
                                // redirect to snaps activity
                                redirectToSnaps();
                            }else {
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                actionButton.setEnabled(true);
                            }
                        }
                    });
        }
    }

    protected void alternateState() {
        if(loginState) {
            setSignupState();
        }else {
            setLoginState();
        }
    }

    private void setLoginState() {
        loginState = true;
        actionButton.setText(R.string.login);
        alternateTextView.setText(R.string.signup);
        alternatePrefixTextView.setText(R.string.signup_prefix);
    }

    private void setSignupState() {
        loginState = false;
        actionButton.setText(R.string.signup);
        alternateTextView.setText(R.string.login);
        alternatePrefixTextView.setText(R.string.login_prefix);
    }

    private void redirectToSnaps() {
        startActivity(new Intent(MainActivity.this, SnapsActivity.class));
    }

    private void handleException(Exception e, String title) {
        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        actionButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
