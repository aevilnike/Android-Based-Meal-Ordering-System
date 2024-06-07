package com.example.foodorderingworkplace.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.activities.user.ShopDetailsActivity;
import com.example.foodorderingworkplace.models.ModelShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop>{

    private Context context;
    public ArrayList<ModelShop> shopeList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopeList) {
        this.context = context;
        this.shopeList = shopeList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent, false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //Get data
        ModelShop modelShop = shopeList.get(position);
        final String uid = modelShop.getUid();
        String email = modelShop.getEmail();
        String name = modelShop.getName();
        String shopName = modelShop.getShopName();
        String phone = modelShop.getPhone();
        String country = modelShop.getCountry();
        String state = modelShop.getState();
        String city = modelShop.getCity();
        String address = modelShop.getAddress();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String timestamp = modelShop.getTimestamp();
        String accountType = modelShop.getAccountType();
        String online = modelShop.getOnline();
        String shopOpen = modelShop.getShopOpen();
        String profileImage = modelShop.getProfileImage();

        loadReviews(modelShop, holder);//load avg rating, set to ratingbar

        //set data
        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.emailTv.setText(email);
        //check if online
        if (online.equals("true")) {
            //shop owner is online
            holder.onlineIv.setVisibility(View.VISIBLE);
        }
        else {
            //shop owner is offline
            holder.onlineIv.setVisibility(View.GONE);
            holder.itemView.setClickable(false);
            holder.itemView.setEnabled(false);
        }
        //check if shop open
        if (online.equals("true")) {
            //shop open
            holder.shopClosedTv.setVisibility(View.GONE);
        }
        else {
            //shope closed
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.baseline_store_mall_directory_grey).into(holder.shopIv);
        }
        catch (Exception e) {
            holder.shopIv.setImageResource(R.drawable.baseline_store_mall_directory_grey);
        }

        //handle click listener, show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews(ModelShop modelShop, final HolderShop holder) {

        String shopUid = modelShop.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue()); // exp 4.3
                            ratingSum = ratingSum + rating; //for avg rating, add all ratings then will divide it by number of reviews

                        }
                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;
                        holder.ratingBar.setRating(avgRating);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopeList.size();
    }

    //view holder
    class HolderShop extends RecyclerView.ViewHolder{

        //ui views of row_shop.xml
        private ImageView shopIv, onlineIv;
        private TextView shopClosedTv, shopNameTv, phoneTv, emailTv;
        private RatingBar ratingBar;
        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //init uid views
            shopIv = itemView.findViewById(R.id.shopIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
