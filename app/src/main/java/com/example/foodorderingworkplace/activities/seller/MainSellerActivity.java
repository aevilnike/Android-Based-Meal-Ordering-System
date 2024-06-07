package com.example.foodorderingworkplace.activities.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingworkplace.activities.LoginActivity;
import com.example.foodorderingworkplace.activities.SettingsActivity;
import com.example.foodorderingworkplace.activities.ShopReviewsActivity;
import com.example.foodorderingworkplace.adapters.AdapterFoodSeller;
import com.example.foodorderingworkplace.Constants;
import com.example.foodorderingworkplace.adapters.AdapterOrderShop;
import com.example.foodorderingworkplace.models.ModelFood;
import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.models.ModelOrderShop;
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

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameTv,shopNameTv,emailTv,tabFoodsTv,tabOrdersTv,filteredFoodTv,filterOrdersTv;
    private EditText searchFoodEt;
    private ImageButton logoutBtn,editProfileBtn,addProductBtn,filterFoodBtn,filterOrderBtn,reviewsBtn,settingsBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ImageView profileIv;
    private RelativeLayout foodsRl,ordersRl;
    private ArrayList<ModelFood> foodList;
    private AdapterFoodSeller adapterFoodSeller;
    RecyclerView foodsRv,ordersRv;
    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTv = findViewById(R.id.nameTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        profileIv = findViewById(R.id.profileIv);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        tabFoodsTv = findViewById(R.id.tabFoodsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        foodsRl = findViewById(R.id.foodsRl);
        ordersRl = findViewById(R.id.ordersRl);
        filteredFoodTv = findViewById(R.id.filteredFoodTv);
        searchFoodEt = findViewById(R.id.searchFoodEt);
        filterFoodBtn = findViewById(R.id.filterFoodBtn);
        foodsRv = findViewById(R.id.foodsRv);
        filterOrdersTv = findViewById(R.id.filterOrdersTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        ordersRv = findViewById(R.id.ordersRv);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        showFoodsUI();
        loadAllFoods();
        loadAllOrders();

        //search
        searchFoodEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterFoodSeller.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make offline
                makeMeOffline();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
            }
        });
        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit add product activity
                startActivity(new Intent(MainSellerActivity.this, AddFoodActivity.class));
            }
        });
        tabFoodsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load foods
                showFoodsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders
                showOrdersUI();
            }
        });
        filterFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.foodoptions1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.foodoptions1[which];
                                filteredFoodTv.setText(selected);
                                if (selected.equals("All")) {
                                    //load all
                                    loadAllFoods();
                                } else {
                                    loadFilteredFood(selected);
                                }
                            }
                        }).show();
            }
        });
        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //option to display in dialog
                String[] options = {"All", "In Progress", "Completed", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //All clicked
                                    filterOrdersTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter("");//show all orders
                                } else {
                                    String optionClicked = options[which];
                                    filterOrdersTv.setText("Showing "+optionClicked+" Orders"); //Show option filter orders
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        }).show();
            }
        });
        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open same reviews activity as used in user main page
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
                startActivity(intent);
            }
        });

        //start settings screen
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));
            }
        });
    }

    private void loadAllOrders() {
        //init aray list
        orderShopArrayList = new ArrayList<>();

        //load orders of shop
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding new data in it
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            //add to list
                            orderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopArrayList);
                        //set adapter to recyclerview
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadFilteredFood(String selected) {
        foodList = new ArrayList<>();
        //get all foods
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Foods")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        foodList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String foodCategory = ""+ds.child("foodCategory").getValue();
                            //if selected category matches food category then add in list
                            if (selected.equals(foodCategory)) {
                                ModelFood modelFood = ds.getValue(ModelFood.class);
                                foodList.add(modelFood);
                            }
                        }
                        //setup adapter
                        adapterFoodSeller = new AdapterFoodSeller(MainSellerActivity.this, foodList);
                        //set adapter
                        foodsRv.setAdapter(adapterFoodSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadAllFoods() {
        foodList = new ArrayList<>();
        //get all foods
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Foods")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        foodList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelFood modelFood = ds.getValue(ModelFood.class);
                            foodList.add(modelFood);
                        }
                        //setup adapter
                        adapterFoodSeller = new AdapterFoodSeller(MainSellerActivity.this, foodList);
                        //set adapter
                        foodsRv.setAdapter(adapterFoodSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void showFoodsUI() {
        //show food ui and hide order ui
        foodsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabFoodsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabFoodsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI() {
        //show order ui and hide food ui
        ordersRl.setVisibility(View.VISIBLE);
        foodsRl.setVisibility(View.GONE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabFoodsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabFoodsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
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
                        Toast.makeText(MainSellerActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
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
                            //get data from db
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            //set data to ui
                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.baseline_person_grey).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.baseline_person_grey);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



}