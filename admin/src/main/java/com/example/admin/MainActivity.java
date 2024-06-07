package com.example.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fbFb,fbGogle,fbTwitter;
    float v = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.view_pager);
        fbGogle = findViewById(R.id.fab_google);
        fbFb = findViewById(R.id.fab_fb);
        fbTwitter = findViewById(R.id.fab_twitter);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        tabLayout.addTab(tabLayout.newTab().setText("Login"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final LoginAdapter adapter = new LoginAdapter(getSupportFragmentManager(), this, tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        fbFb.setTranslationY(300);
        fbGogle.setTranslationY(300);
        fbTwitter.setTranslationY(300);

        fbFb.setAlpha(v);
        fbGogle.setAlpha(v);
        fbTwitter.setAlpha(v);

        fbFb.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        fbGogle.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(600).start();
        fbTwitter.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(800).start();
        fbTwitter.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(100).start();

    }

}