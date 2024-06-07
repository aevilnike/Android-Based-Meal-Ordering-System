package com.example.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainAdminActivity extends AppCompatActivity {

    TextView registerTv,employeeCountTv,sellerCountTv,payoutCountTv;
    RecyclerView payoutRv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ImageButton logoutBtn;
    private ArrayList<PayoutRequestModel> payoutList;
    private PayoutRequestAdapter payoutRequestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        registerTv = findViewById(R.id.registerTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        payoutRv = findViewById(R.id.payoutRv);
        employeeCountTv = findViewById(R.id.employeeCountTv);
        sellerCountTv = findViewById(R.id.sellerCountTv);
        payoutCountTv = findViewById(R.id.payoutCountTv);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadPayout();
        loadCounts();
        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register user
                // Intent to open RegisterUser activity
                Intent intent = new Intent(MainAdminActivity.this, RegisterUser.class);
                startActivity(intent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make offline
                makeMeOffline();
            }
        });

    }

    private void loadCounts() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference sellerRef = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference payoutRef = FirebaseDatabase.getInstance().getReference("PayoutRequests");

        // Employee count
        userRef.orderByChild("accountType").equalTo("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long employeeCount = snapshot.getChildrenCount();
                employeeCountTv.setText(String.valueOf(employeeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Seller count
        sellerRef.orderByChild("accountType").equalTo("Seller").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long sellerCount = snapshot.getChildrenCount();
                sellerCountTv.setText(String.valueOf(sellerCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Payout request count
        payoutRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long payoutCount = snapshot.getChildrenCount();
                payoutCountTv.setText(String.valueOf(payoutCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
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
                        Toast.makeText(MainAdminActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(MainAdminActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainAdminActivity.this, MainActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadPayout() {
        //init list
        payoutList = new ArrayList<>();
        DatabaseReference payoutRef = FirebaseDatabase.getInstance().getReference("PayoutRequests");
        payoutRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                payoutList.clear(); // Clear existing data
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PayoutRequestModel payoutRequest = ds.getValue(PayoutRequestModel.class);
                    //fetch seller name and profile image
                    loadSellerInfo(payoutRequest);
                    payoutList.add(payoutRequest);
                }
                //setup adapter
                payoutRequestAdapter = new PayoutRequestAdapter(MainAdminActivity.this, payoutList);
                //set adapter
                payoutRv.setAdapter(payoutRequestAdapter);
                // Set click listeners for buttons
                payoutRequestAdapter.setOnItemClickListener(new PayoutRequestAdapter.OnItemClickListener() {
                    @Override
                    public void onAcceptClick(int position) {
                        // Handle accept button click, update Firebase
                        // You can access the specific payout request using payoutList.get(position)
                        // Update Firebase accordingly
                        handleAcceptButtonClick(position);
                        Toast.makeText(MainAdminActivity.this, "AcceptButton Clicked", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRejectClick(int position) {
                        // Handle reject button click, update Firebase
                        // You can access the specific payout request using payoutList.get(position)
                        // Update Firebase accordingly
                        handleRejectButtonClick(position);
                        Toast.makeText(MainAdminActivity.this, "RejectButton Clicked", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void loadSellerInfo(PayoutRequestModel payoutRequest) {
        DatabaseReference sellerRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(payoutRequest.getSellerUid());

        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Set seller's name and profile image in the PayoutRequestModel
                    payoutRequest.setSellerName(snapshot.child("name").getValue(String.class));
                    payoutRequest.setSellerProfileImage(snapshot.child("profileImage").getValue(String.class));

                    // Notify the adapter that the data has changed
                    payoutRequestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    private void handleAcceptButtonClick(int position) {
        // Access the specific payout request using payoutList.get(position)
        PayoutRequestModel payoutRequest = payoutList.get(position);

        // Update the seller's payout value in the Firebase database
        DatabaseReference sellerPayoutRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(payoutRequest.getSellerUid())
                .child("payout");

        // Deduct the amount value
        sellerPayoutRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the current value
                Double currentPayout = snapshot.getValue(Double.class);

                if (currentPayout != null) {
                    // Deduct the amount value
                    sellerPayoutRef.setValue(currentPayout - payoutRequest.getAmount());
                    // Delete the payout request from the Firebase database
                    DatabaseReference payoutRef = FirebaseDatabase.getInstance().getReference("PayoutRequests")
                            .child(payoutRequest.getPayoutId());

                    payoutRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

    }

    private void handleRejectButtonClick(int position) {
        // Access the specific payout request using payoutList.get(position)
        PayoutRequestModel payoutRequest = payoutList.get(position);

        // Delete the payout request from the Firebase database
        DatabaseReference payoutRef = FirebaseDatabase.getInstance().getReference("PayoutRequests")
                .child(payoutRequest.getPayoutId());

        payoutRef.removeValue();

    }
    private void loadMyInfo() {
        //init list

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data from db
                            /*String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();*/

                            //set data to ui
                            //nameTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}