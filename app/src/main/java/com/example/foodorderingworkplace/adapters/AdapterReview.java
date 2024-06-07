package com.example.foodorderingworkplace.adapters;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.models.ModelReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview>{

    private Context context;
    private ArrayList<ModelReview> reviewsArrayList;

    public AdapterReview(Context context, ArrayList<ModelReview> reviewsArrayList) {
        this.context = context;
        this.reviewsArrayList = reviewsArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_review
        View view = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        //get data at position
        ModelReview modelReview = reviewsArrayList.get(position);
        String uid = modelReview.getUid();
        String ratings = modelReview.getRatings();
        String timestamp = modelReview.getTimestamp();
        String review = modelReview.getReview();

        //get user info (profile,name) who wrote the review using their uid
        loadUserDetail(modelReview, holder);

        //convert timestamp to proper date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(Long.parseLong(timestamp)));
        holder.dateTv.setText(formattedDate);
        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTv.setText(review);
    }

    private void loadUserDetail(ModelReview modelReview, HolderReview holder) {
        //uid user who write review
        String uid = modelReview.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user info
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        //set data
                        holder.nameTv.setText(name);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.baseline_person_grey).into(holder.profileIv);
                        } catch (Exception e) {
                            //if anything goes wrong set default image
                            holder.profileIv.setImageResource(R.drawable.baseline_person_grey);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return reviewsArrayList.size();
    }

    //view holder class, holds/inits views of recyclerview
    class HolderReview extends RecyclerView.ViewHolder {
        private ImageView profileIv;
        private TextView nameTv,dateTv,reviewTv;
        private RatingBar ratingBar;
        //ui view of layout row_reviews
        public HolderReview(@NonNull View itemView) {
            super(itemView);

            //init views of row_reviews
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            reviewTv = itemView.findViewById(R.id.reviewTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);


        }
    }
}
