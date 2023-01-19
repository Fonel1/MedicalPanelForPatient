package com.test.medicalpanel2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.Fragments.HomeFragment;
import com.test.medicalpanel2.Fragments.ProfileFragment;
import com.test.medicalpanel2.Model.User;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);


        reference = FirebaseFirestore.getInstance().collection("Users").document(user.getEmail());
        reference
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if (task.isSuccessful()) {
                                                   FirebaseMessaging.getInstance().getToken()
                                                           .addOnCompleteListener(new OnCompleteListener<String>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<String> task) {
                                                                   Common.updateToken(getBaseContext(), task.getResult());
                                                                   String token = task.getResult();
                                                                   Log.d("MPToken ", token);
                                                               }
                                                           });

                                                   reference.
                                                           get()
                                                           .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                   if (task.isSuccessful()) {
                                                                       DocumentSnapshot userSnapShot = task.getResult();
                                                                       if (!userSnapShot.exists()) {
                                                                           //
                                                                       } else {
                                                                           Common.currentUser = userSnapShot.toObject(User.class);
                                                                       }
                                                                   }
                                                               }
                                                           });
                                               }
                                           }
                                       });


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_home)
                    fragment = new HomeFragment();
                else if (item.getItemId() == R.id.action_profile)
                    fragment = new ProfileFragment();
                return loadFragment(fragment);
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.action_home);

        if (user != null)
        {
            Paper.init(HomeActivity.this);
            Paper.book().write(Common.LOGGED_KEY, user.getEmail());
        }

    }

    public boolean loadFragment(Fragment fragment) {
        if (fragment != null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Na pewno chcesz się wylogować?")
                .setCancelable(false)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        user = null;
                        Common.step = -1;
                        Common.city = "";
                        Common.currentUser = null;
                        Common.currentDoctor = null;
                        Common.currentAppointment = null;
                        Common.currentClinic = null;
                        Common.currentData = null;
                        Common.currentAppointmentId = "";
                        Common.currentDataSlot = -1;

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();


                    }
                }).setNegativeButton("Nie", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

}