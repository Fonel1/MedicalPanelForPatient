package com.test.medicalpanel2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.Interface.IDoctorsLoadListener;
import com.test.medicalpanel2.Interface.ITestNameLoadListener;
import com.test.medicalpanel2.Model.Doctor;
import com.test.medicalpanel2.Model.FCMResponse;
import com.test.medicalpanel2.Model.FCMSendData;
import com.test.medicalpanel2.Model.MyToken;
import com.test.medicalpanel2.Model.Notification;
import com.test.medicalpanel2.Model.SentTestInformation;
import com.test.medicalpanel2.Retrofit.IFCMApi;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SendTestResultToDoctor extends AppCompatActivity implements ITestNameLoadListener, IDoctorsLoadListener {

    CollectionReference testName;
    CollectionReference doctors;

    ITestNameLoadListener iTestNameLoadListener;
    IDoctorsLoadListener iDoctorsLoadListener;

    SimpleDateFormat simpleDateFormat;

    private Uri imageUri;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IFCMApi ifcmApi;

    @BindView(R.id.spinner_test_results)
    MaterialSpinner spinner_test;
    @BindView(R.id.spinner_doctors)
    MaterialSpinner spinner_doctors;
    @BindView(R.id.img_patient_test_results)
    ImageView imgPatient;
    @BindView(R.id.btn_send)
    Button btn_send;
    private ProgressDialog progressDialog;

    private StorageReference storageReference;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_test_result_to_doctor);
        ButterKnife.bind(SendTestResultToDoctor.this);

        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        progressDialog = new ProgressDialog(this);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        iTestNameLoadListener = this;
        iDoctorsLoadListener = this;

        loadTestName();

        imgPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeAPicture();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(imageUri);
            }
        });
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri != null)
        {
            progressDialog.setMessage("Wysyłanie");
            progressDialog.show();
            String fileName = Common.getFileName(getContentResolver(), imageUri);
            String path = new StringBuilder("Przesłane_Wyniki/")
                    .append(fileName)
                    .append("_"+Common.currentUser.getEmail()
                            +"_"+Common.simpleDateFormat.format(new Date())
                            +"_"+Common.test)
                    .toString();
            storageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = storageReference.putFile(imageUri);

            Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                        Toast.makeText(SendTestResultToDoctor.this, "Nieudane wysyłanie", Toast.LENGTH_SHORT).show();
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        String url = task.getResult().toString()
                                .substring(0,
                                        task.getResult().toString().indexOf("&token"));
                        storageImage(url);
                        Log.d("Link: ", url);

                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(SendTestResultToDoctor.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(this, "Błąd", Toast.LENGTH_SHORT).show();
        }
    }

    private void storageImage(String url) {

        SentTestInformation testInformation = new SentTestInformation();

        testInformation.setDoctorName(Common.currentDoctorT);
        testInformation.setDone(false);
        testInformation.setPatientEmail(Common.currentUser.getEmail());
        testInformation.setPatientName(Common.currentUser.getName());
        testInformation.setTime(simpleDateFormat.format(new Date()));
        testInformation.setTestName(Common.test);
        testInformation.setPhoto(url);
        testInformation.setSummaryTxt("");

        db.collection("TestResults")
                .document(Common.test)
                .collection("Doctors")
                .document(Common.currentDoctorT)
                .collection("ReceivedTests")
                .document()
                .set(testInformation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            progressDialog.dismiss();
                            submitToUser(testInformation);
                            
                            Toast.makeText(SendTestResultToDoctor.this, "Wysłano", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SendTestResultToDoctor.this, HomeActivity.class));

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(SendTestResultToDoctor.this, "Błąd"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void submitToUser(SentTestInformation testInformation) {
        CollectionReference userTest = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentUser.getEmail())
                .collection("SendTestResults");

        userTest.document().set(testInformation);
    }

    private void takeAPicture() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(SendTestResultToDoctor.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(SendTestResultToDoctor.this, "Brak Zgody", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(SendTestResultToDoctor.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1 );
            } else
            {
                chooseImage();
            }
        } else {
            chooseImage();
        }
    }

    private void chooseImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SendTestResultToDoctor.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK) {
                imageUri = result.getUri();
                imgPatient.setImageURI(imageUri);
            } else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void loadTestName() {

        testName = FirebaseFirestore.getInstance()
                .collection("TestResults");

        testName
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            List<String> list = new ArrayList<>();
                            list.add("Wybierz badanie do przesłania");
                            for (QueryDocumentSnapshot documentSnapshot:task.getResult())
                                list.add(documentSnapshot.getId());
                            iTestNameLoadListener.onTestResultLoadSuccess(list);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iTestNameLoadListener.onTestResultLoadFailed(e.getMessage());
            }
        });
    }

    @Override
    public void onTestResultLoadSuccess(List<String> testNamesList) {
        spinner_test.setItems(testNamesList);
        spinner_test.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position > 0)
                {
                    loadSpinnerDoctor(item.toString());
                    spinner_doctors.setEnabled(true);
                } else
                {
                    spinner_doctors.setEnabled(false);
                }
            }
        });
    }

    private void loadSpinnerDoctor(String testName) {
        Common.test = testName;

        doctors = FirebaseFirestore.getInstance()
                .collection("TestResults")
                .document(testName)
                .collection("Doctors");

        doctors
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    List<String> list = new ArrayList<>();
                    list.add("WYbierz lekarza");
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult())
                        list.add(documentSnapshot.getId());
                    iDoctorsLoadListener.onDoctorsLoadSuccess(list);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iDoctorsLoadListener.onDoctorsLoadFailed(e.getMessage());
            }
        });
    }

    @Override
    public void onTestResultLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDoctorsLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDoctorsLoadSuccess(List<String> doctorsList) {
        spinner_doctors.setItems(doctorsList);
        spinner_doctors.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position > 0)
                {
                    Common.currentDoctorT = item.toString();
                    imgPatient.setVisibility(View.VISIBLE);
                    Toast.makeText(SendTestResultToDoctor.this, ""+Common.currentDoctorT, Toast.LENGTH_SHORT).show();
                } else
                {
                    imgPatient.setVisibility(View.GONE);
                }
            }
        });
    }


}