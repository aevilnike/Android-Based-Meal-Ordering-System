package com.example.foodorderingworkplace.activities.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class ShopUserFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_shop_user, null);

        nameTv = v.findViewById(R.id.nameTv);
        emailTv = v.findViewById(R.id.emailTv);
        employeeIdTv = v.findViewById(R.id.employeeIdTv);
        phoneTv = v.findViewById(R.id.phoneTv);
        tabShopTv = v.findViewById(R.id.tabShopTv);
        nameTv = v.findViewById(R.id.nameTv);
        tabOrdersTv = v.findViewById(R.id.tabOrdersTv);
        logoutBtn = v.findViewById(R.id.logoutBtn);
        profileIv = v.findViewById(R.id.profileIv);
        shopsRl = v.findViewById(R.id.shopsRl);
        ordersRl = v.findViewById(R.id.ordersRl);
        shopsRv = v.findViewById(R.id.shopsRv);
        ordersRv = v.findViewById(R.id.ordersRv);
        settingsBtn = v.findViewById(R.id.settingsBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getContext());
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
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });
        return v;
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
                        adapterShop = new AdapterShop(getContext(), shopsList);
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
        tabShopTv.setBackgroundResource(R.drawable.shape_rect08);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if the flag is set
        if (getActivity().getIntent().getBooleanExtra("shouldShowOrdersUI", false)) {
            // Clear the flag to avoid calling showOrdersUI() again next time
            getActivity().getIntent().removeExtra("shouldShowOrdersUI");
            // Call the method when the activity is resumed
            showOrdersUI();
        }
    }


    private void showOrdersUI() {
        //show/hides shops ui
        if (tabShopTv != null && ordersRl != null && shopsRl != null) {
            ordersRl.setVisibility(View.VISIBLE);
            shopsRl.setVisibility(View.GONE);

            tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
            tabOrdersTv.setBackgroundResource(R.drawable.shape_rect08);

            tabShopTv.setTextColor(getResources().getColor(R.color.colorWhite));
            tabShopTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
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
                        Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
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
                                        adapterOrderUser = new AdapterOrderUser(getContext(), orderList);
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