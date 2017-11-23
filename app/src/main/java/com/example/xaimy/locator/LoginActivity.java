package com.example.xaimy.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
public class LoginActivity extends AppCompatActivity
{
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnLogin, btnReset;
    public static final int RequestPermissionCode = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null)
        {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // set the view now
        setContentView(R.layout.activity_login);

       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (Button) findViewById(R.id.btn_reset_password);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(checkPermission()){

                    Toast.makeText(LoginActivity.this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();

                }
                else
                {

                    requestPermission();
                }
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();
                if (TextUtils.isEmpty(email))
                {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                    /*Snackbar snackbar = Snackbar.make(findViewById(R.id.loginCordinate),"Enter Email Address :", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.GREEN);
                    snackbar.show();*/
                }

                if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                    /*Snackbar snackbar = Snackbar.make(findViewById(R.id.loginCordinate),"Enter Password :", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.GREEN);
                    snackbar.show();*/
                }
                //to save the value of email locally
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("EmailID",inputEmail.getText().toString());
                editor.apply();
                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful())
                                {
                                    // there was an error
                                    if (password.length() < 6)
                                    {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else
                                    {
                                        Snackbar snackbar = Snackbar
                                                .make(findViewById(R.id.loginCordinate),getString(R.string.auth_failed), Snackbar.LENGTH_LONG)
                                                .setAction("Reset Passowrd", new View.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(View view)
                                                    {
                                                        startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
                                                        finish();
                                                    }
                                                });

                                        snackbar.show();
                                        /*Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();*/
                                    }
                                } else
                                {
                                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(LoginActivity.this, new String[]
                {
                        ACCESS_FINE_LOCATION,
                        READ_CONTACTS,
                        READ_PHONE_STATE
                }, RequestPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean LocationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadContactsPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadPhoneStatePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (LocationPermission && ReadContactsPermission && ReadPhoneStatePermission) {

                        /*Toast.makeText(LoginActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();*/
                    }
                    else {
                        Toast.makeText(LoginActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;
    }



}
