package com.example.foodorderingworkplace.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.adapters.AdapterReview;
import com.example.foodorderingworkplace.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    private  String shopUid;
    private ImageButton backBtn;
    private TextView shopNameTv,ratingsTv;
    private ImageView profileIv;
    private RatingBar ratingBar;
    private RecyclerView reviewsRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterReview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        backBtn = findViewById(R.id.backBtn);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingsTv = findViewById(R.id.ratingsTv);
        profileIv = findViewById(R.id.profileIv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewsRv = findViewById(R.id.reviewsRv);

        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails(); //for shop name, image
        loadReviews();//for reviews list, avg rating

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
    private float ratingSum = 0;
    private void loadReviews() {
        //init list
        reviewArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it
                        reviewArrayList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue()); // exp 4.3
                            ratingSum = ratingSum + rating; //for avg rating, add all ratings then will divide it by number of reviews

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }
                        //setup adapter
                        adapterReview = new AdapterReview(ShopReviewsActivity.this, reviewArrayList);
                        //set to recyclerview
                        reviewsRv.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingsTv.setText(String.format("%.2f", avgRating) + "["+numberOfReviews+"]");
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String shopImage = ""+snapshot.child("profileImage").getValue();
                        //set shop info to ui
                        shopNameTv.setText(shopName);
                        try {
                            Picasso.get().load(shopImage).placeholder(R.drawable.baseline_store_mall_directory_grey).into(profileIv);
                        } catch (Exception e) {
                            profileIv.setImageResource(R.drawable.baseline_store_mall_directory_grey);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}