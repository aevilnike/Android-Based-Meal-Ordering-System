package com.example.foodorderingworkplace.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.activities.seller.MainSellerActivity;
import com.example.foodorderingworkplace.activities.user.HomeUserFragment;
import com.example.foodorderingworkplace.activities.user.MainUserActivity;
import com.example.foodorderingworkplace.activities.user.ShopUserFragment;
import com.example.foodorderingworkplace.activities.user.UserBottomNaviActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make fullscreen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        image = findViewById(R.id.logo_image);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        //start login activity after 2sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    //user not logged in start login activity
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is loggin, check user type
                    checkUserType();
                }
            }
        }, 2000);
    }
    private void checkUserType() {
        //if user is seller, start seller main
        //if user is buyer, start user main screen

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String accountType = ""+snapshot.child("accountType").getValue();
                            if (accountType.equals("Seller")) {
                                progressDialog.dismiss();
                                //user is seller
                                startActivity(new Intent(SplashActivity.this, MainSellerActivity.class));
                                finish();
                            } else {
                                progressDialog.dismiss();
                                //user is buyer
                                startActivity(new Intent(SplashActivity.this, UserBottomNaviActivity.class));
                                finish();
                            }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}