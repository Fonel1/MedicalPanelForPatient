package com.test.medicalpanel2.Common;



import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.medicalpanel2.Model.AppointmentInformation;
import com.test.medicalpanel2.Model.Clinic;
import com.test.medicalpanel2.Model.Doctor;
import com.test.medicalpanel2.Model.MyToken;
import com.test.medicalpanel2.Model.User;
import com.test.medicalpanel2.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import io.paperdb.Paper;

public class Common {

    public static final String KEY_ENABLE_BUTTON_NEXT = "ENABLE_BUTTON_NEXT";
    public static final String KEY_CLINIC_STORE = "CLINIC_SAVE";
    public static final String KEY_DOCTOR_LOAD_DONE = "DOCTOR_LOAD_DONE";
    public static final String KEY_DISPLAY_DATA_SLOT = "DISPLAY_DATA_SLOT";
    public static final String KEY_STEP = "STEP";
    public static final String KEY_DOCTOR_SELECTED = "DOCTOR_SELECTED";
    public static final int DATA_SLOT_TOTAL = 16; //how many terms doctor has in a day
    public static final Object DISABLE_TAG = "DISABLE";
    public static final String KEY_DATA_SLOT = "TIME_SLOT";
    public static final String KEY_CONFIRM_APPOINTMENT = "CONFIRM_APPOINTMENT";
    public static final String EVENT_URI_CACHE = "URI_EVENT_SAVE";
    public static final String LOGGED_KEY = "UserLogged";
    public static final String TITLE_KEY = "title";
    public static final String CONTENT_KEY = "content";
    public static Clinic currentClinic;
    public static int step = 0;
    public static String city = "";
    public static Doctor currentDoctor;
    public static int currentDataSlot =-1;
    public static Calendar currentData = Calendar.getInstance();
    public static User currentUser = new User();
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); //only in case format key is needed
    public static AppointmentInformation currentAppointment;
    public static String currentAppointmentId="";
    public static String test = "";
    public static String currentDoctorT = "";

    public static String convertDataSlotToString(int slot) {
        switch (slot)
        {
            case 0:
                return "9:00-9:30";
            case 1:
                return "9:30-10:00";
            case 2:
                return "10:00-10:30";
            case 3:
                return "10:30-11:00";
            case 4:
                return "11:00-11:30";
            case 5:
                return "11:30-12:00";
            case 6:
                return "12:00-12:30";
            case 7:
                return "12:30-13:00";
            case 8:
                return "13:00-13:30";
            case 9:
                return "13:30-14:00";
            case 10:
                return "14:00-14:30";
            case 11:
                return "14:30-15:00";
            case 12:
                return "15:00-15:30";
            case 13:
                return "15:30-16:00";
            case 14:
                return "16:00-16:30";
            case 15:
                return "16:30-17:00";
            default:
                return "0";
        }
    }

    public static void showNotifications(Context context, int noti_id, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;
        if (intent != null)
        {
            pendingIntent = PendingIntent.getActivity(context,
                    noti_id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFICATION_CHANNEL_ID = "MedPan_channel_2";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "MedPan", NotificationManager.IMPORTANCE_DEFAULT);

                notificationChannel.setDescription("Patient app");
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(false);

                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

            builder.setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(false)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

            if (pendingIntent != null)
            {
                builder.setContentIntent(pendingIntent);
                Notification notification = builder.build();

                notificationManager.notify(noti_id, notification);

            }
        }
    }



    public static String convertTimeStampToStringKey(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        return simpleDateFormat.format(date);
    }

    @SuppressLint("Range")
    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String result = null;
        if (fileUri.getScheme().equals("content"))
        {
            Cursor cursor = contentResolver.query(fileUri, null, null, null, null);
            try{
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                cursor.close();
            }
        }
        if (result == null)
        {
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut+1);
        }
        return result;
    }

    public static enum TOKEN_TYPE{
        PATIENT,
        DOCTOR
    }

    public static void updateToken(Context context, String t) {


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            MyToken myToken = new MyToken();
            myToken.setToken(t);
            myToken.setTokenType(TOKEN_TYPE.PATIENT);
            myToken.setUserEmail(user.getEmail());

            FirebaseFirestore.getInstance()
                    .collection("Tokens")
                    .document(Objects.requireNonNull(user.getEmail()))
                    .set(myToken)
                    .addOnCompleteListener(task -> {

                    });
        } else
        {
            Paper.init(context);
            String pUser = Paper.book().read(Common.LOGGED_KEY);
            if (pUser != null)
            {
                if (!TextUtils.isEmpty(pUser))
                {
                    MyToken myToken = new MyToken();
                    myToken.setToken(t);
                    myToken.setTokenType(TOKEN_TYPE.PATIENT);
                    myToken.setUserEmail(pUser);

                    FirebaseFirestore.getInstance()
                            .collection("Tokens")
                            .document(pUser)
                            .set(myToken)
                            .addOnCompleteListener(task -> {

                            });
                }
            }
        }
    }
}
