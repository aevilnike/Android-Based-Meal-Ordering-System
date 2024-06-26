package com.example.foodorderingworkplace.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingworkplace.Constants;
import com.example.foodorderingworkplace.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat fcmSwitch;
    ImageButton backBtn;
    private TextView notificationStatusTv;
    private static final String enabledMessage = "Notifications are enabled";
    private static final String disabledMessage = "Notifications are disabled";
    private boolean isChecked = false;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //add option in main seller and main user activity to open settings activity

        fcmSwitch = findViewById(R.id.fcmSwitch);
        backBtn = findViewById(R.id.backBtn);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);

        firebaseAuth = FirebaseAuth.getInstance();
        //init shared preferences
        sp = getSharedPreferences("SETTINGS_SP", MODE_PRIVATE);
        //check last selected option; true/false
        isChecked = sp.getBoolean("FCM_ENABLED", false);
        fcmSwitch.setChecked(isChecked);
        if (isChecked) {
            //was enabled
            notificationStatusTv.setText(enabledMessage);
        } else {
            //was disabled
            notificationStatusTv.setText(disabledMessage);
        }
        //handle click goback
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //add switch check change listener to enable disable notifications
        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //checked, enable notifications
                    subscribeToTopic();
                } else {
                    //unchecked, disable notifications
                    unsubscribeToTopic();
                }
            }
        });
    }

    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //subscribe successfully
                        //save setting ins shared preferences
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", true);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed subscribe
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void unsubscribeToTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //unsubscribe successfully
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", false);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed unsubscribe
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}