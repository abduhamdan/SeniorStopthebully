package com.SeniorBullyingApp.tabview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class LoginPage extends AppCompatActivity implements View.OnClickListener {
private Button signupscreen;
private Button loginButton;
private FirebaseAuth fAuth;
private EditText email, password;
private ProgressBar loginProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpageforapplication);
        signupscreen=(Button) findViewById(R.id.donthaveanaccountbutton);
        signupscreen.setOnClickListener(this);

        loginButton=(Button) findViewById(R.id.logintotheAppButton);
        fAuth=FirebaseAuth.getInstance();
        email= (EditText) findViewById(R.id.editTextTextPersonName4);
        password= (EditText) findViewById( R.id.editTextTextPassword2);
        loginProgressBar=(ProgressBar) findViewById(R.id.loginProgress);
        loginButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.donthaveanaccountbutton:
               // finish();
                 Intent w = new Intent(this, Registration.class);
                 startActivity(w);
                 break;

            case R.id.logintotheAppButton:
                userLogin();
                break;

        }
    }

    protected void onStart() {


        super.onStart();

        if (fAuth.getCurrentUser()!=null)
            {
                finish();
                startActivity(new Intent (this, MainActivity.class));
            }
        }

    private void userLogin()
    {
        String ema= email.getText().toString();
        String pass= password.getText().toString();
        if (ema.isEmpty()){
            email.setError("Email is required");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(ema).matches()) //checks for invalid email
        {
            email.setError("Please enter a valid email");
            email.requestFocus();
            return;
        }
        if (pass.isEmpty()){
            password.setError("Password is required");
            password.requestFocus();
            return;}

        if (pass.length()<6){
            email.setError("Please enter a password with a minimum length of 6");
            email.requestFocus();
            return;
        }
        loginProgressBar.setVisibility(View.VISIBLE);
    fAuth.signInWithEmailAndPassword(ema,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            loginProgressBar.setVisibility(View.GONE);
            if(task.isSuccessful())
            {
                finish();
                Intent w =new Intent(getApplicationContext(), MainActivity.class);
                w.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //ensures when we back it doesnt go back to the login
                startActivity(w);
            }
            else{
                if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                {email.setError("Incorrect email or/and password");
                    email.requestFocus();}
                else Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    });
    }
}