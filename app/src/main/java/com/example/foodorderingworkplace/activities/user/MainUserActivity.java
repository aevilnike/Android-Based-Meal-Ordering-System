package com.example.foodorderingworkplace.activities.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.activities.LoginActivity;
import com.example.foodorderingworkplace.activities.SettingsActivity;
import com.example.foodorderingworkplace.adapters.AdapterOrderUser;
import com.example.foodorderingworkplace.adapters.AdapterShop;
import com.example.foodorderingworkplace.models.ModelOrderUser;
import com.example.foodorderingworkplace.models.ModelShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {
    private TextView nameTv,emailTv,employeeIdTv,phoneTv,tabShopTv,tabOrdersTv;
    private ImageButton logoutBtn,editProfileBtn,settingsBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ImageView profileIv;
    private RelativeLayout shopsRl,ordersRl;
    private RecyclerView shopsRv,ordersRv;
    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;
    private ArrayList<ModelOrderUser> orderList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        employeeIdTv = findViewById(R.id.employeeIdTv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopTv = findViewById(R.id.tabShopTv);
        nameTv = findViewById(R.id.nameTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        profileIv = findViewById(R.id.profileIv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);
        shopsRv = findViewById(R.id.shopsRv);
        ordersRv = findViewById(R.id.ordersRv);
        settingsBtn = findViewById(R.id.settingsBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        //after login show shops
        showShopsUI();
        loadShops();
        onResume();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
            }
        });
        tabShopTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show shop
                showShopsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show order
                showOrdersUI();
            }
        });
        //start settings screen
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
            }
        });
    }

    private void loadShops() {
        //init list
        shopsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        shopsList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);
                            shopsList.add(modelShop);
                        }
                        //setup adapter
                        adapterShop = new AdapterShop(MainUserActivity.this, shopsList);
                        //set adapter
                        shopsRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showShopsUI() {
        //show/hides shops ui
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Check if the flag is set
        if (getIntent().getBooleanExtra("shouldShowOrdersUI", false)) {
            // Call the method when the activity is resumed
            showOrdersUI();
            // Clear the flag to avoid calling showOrdersUI() again next time
            getIntent().removeExtra("shouldShowOrdersUI");
        }
    }
    private void showOrdersUI() {
        //show/hides shops ui
        ordersRl.setVisibility(View.VISIBLE);
        shopsRl.setVisibility(View.GONE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabShopTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void makeMeOffline() {
        //after logout, make user offline
        progressDialog.setMessage("Logging Out...");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        firebaseAuth.signOut();
                        checkUser();
                        Toast.makeText(MainUserActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String employeeId = ""+ds.child("employeeId").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            employeeIdTv.setText(employeeId);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.baseline_store_mall_directory_grey).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.baseline_store_mall_directory_grey);
                            }

                            //load shops in the same city with user
                            //loadorders
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Error loading user info: " + error.getMessage());
                    }
                });
    }

    private void loadOrders() {
        //init order list
        orderList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    String uid = ""+ds.getRef().getKey(); //get all uid from Users table
                    //get ref from Users->uid->Orders
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())//compare all User key with Current User until same
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot ds: snapshot.getChildren()) {//loop get all the order on current user
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);
                                            //add to list
                                            orderList.add(modelOrderUser); //put into orderList array
                                        }
                                        //setup adapter
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, orderList);
                                        //set to recyclerview
                                        ordersRv.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}