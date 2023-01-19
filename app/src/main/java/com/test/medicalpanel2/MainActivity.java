package com.test.medicalpanel2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.Model.User;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";
    TextInputEditText Email;
    TextInputEditText Password;
    TextView btnSignUp;
    Button btnLogin;

    FirebaseAuth mAuth;
    DocumentReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Email = findViewById(R.id.inputEmail);
        Password = findViewById(R.id.inputPassword);
        btnSignUp = findViewById(R.id.SignUp);
        btnLogin = findViewById(R.id.btn_Login);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> {
            loginUser();
        });
        btnSignUp.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        Dexter.withActivity(this)
                .withPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                }).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted())
                {
                    btnLogin.setEnabled(true);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();

    }

    private void loginUser(){
        String email = Objects.requireNonNull(Email.getText()).toString();
        String password = Objects.requireNonNull(Password.getText()).toString();

        if (email.isEmpty())
        {
            Email.setError("Email cannot be empty");
            Email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Email.setError("Wprowadz adres email");
            Email.requestFocus();
            return;
        }

        if (password.isEmpty())
        {
            Password.setError("Wprowadz haslo");
            Password.requestFocus();
            return;
        }

        if (password.length() < 6)
        {
            Password.setError("Haslo musi miec minimum 6 znakow");
            Password.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {

                if (task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    assert user != null;
                    //reference = FirebaseFirestore.getInstance().collection("Users").document(Objects.requireNonNull(user.getEmail()));
                    if (user.isEmailVerified())
                    {

                        /*FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        Common.updateToken(getBaseContext(), task.getResult());
                                        String token = task.getResult();
                                        Log.d("MPToken ", token);
                                    }
                                });*/
                        //startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        /*reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot userSnapShot = task.getResult();
                                    if (!userSnapShot.exists()){
                                        //
                                    } else {
                                        Common.currentUser = userSnapShot.toObject(User.class);
                                    }
                                }
                            }
                        });*/

                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(MainActivity.this, "Wymagane potwierdzenie adresu email", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(MainActivity.this, "Błąd: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}