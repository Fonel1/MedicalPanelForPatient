package com.test.medicalpanel2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.medicalpanel2.Model.User;


public class RegisterActivity extends AppCompatActivity {


    public static final String TAG = "TAG";
    EditText edtEmail,edtName, edtPassword, edtAddress, edtPhoneNumber;
    TextView Already;
    Button Reg;

    FirebaseAuth mAuth;
    FirebaseFirestore mStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtAddress = findViewById(R.id.edt_address);
        edtPhoneNumber = findViewById(R.id.edt_phoneNumber);
        edtName = findViewById(R.id.inputName);
        edtEmail = findViewById(R.id.inputEmail);
        edtPassword = findViewById(R.id.inputPassword);
        Already = findViewById(R.id.alreadyHaveAccount);
        Reg = findViewById(R.id.btnRegister);

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Reg.setOnClickListener(view ->{
            createUser();
        });

        Already.setOnClickListener(view ->{
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        });
    }

    private void createUser(){
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString();
        String address = edtAddress.getText().toString();

        if (TextUtils.isEmpty(email)){
            edtEmail.setError("Email cannot be empty");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)){
            edtPassword.setError("Password cannot be empty");
            edtPassword.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            edtName.setError("Full Name is required");
            edtName.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            edtPhoneNumber.setError("Full Name is required");
            edtPhoneNumber.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtEmail.setError("Provide valid email");
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Full Name is required");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Min password length should be 6 characters!");
            edtPassword.requestFocus();
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));

                    User user = new User(name, phoneNumber, address, email);
                    userID = mAuth.getCurrentUser().getEmail();
                    DocumentReference documentReference = mStore.collection("Users").document(userID);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(RegisterActivity.this, "Konto zostało założone!", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(RegisterActivity.this, "Rejestracja nieudana!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}