package com.example.foodorderingworkplace.activities.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.activities.LoginActivity;
import com.example.foodorderingworkplace.adapters.AsiaFoodAdapter;
import com.example.foodorderingworkplace.adapters.PopularFoodAdapter;
import com.example.foodorderingworkplace.models.AsiaFood;
import com.example.foodorderingworkplace.models.PopularFood;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeUserFragment extends Fragment {

    TextView textView;
    private ImageView profileIv;
    RecyclerView popularRecycler, asiaRecycler;
    PopularFoodAdapter popularFoodAdapter;
    AsiaFoodAdapter asiaFoodAdapter;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home_user, null);
        popularRecycler = v.findViewById(R.id.popular_recycler);
        asiaRecycler = v.findViewById(R.id.asia_recycler);
        profileIv = v.findViewById(R.id.profileIv);
        textView = v.findViewById(R.id.textView);

        // now here we will add some dummy data to out model class

        List<PopularFood> popularFoodList = new ArrayList<>();

        popularFoodList.add(new PopularFood("Chiken Drumstick", "RM7.05", R.drawable.p1_roastedchickdrumstick));
        popularFoodList.add(new PopularFood("Pasta with Meat", "RM17.05", R.drawable.p2_pastawithmeat));
        popularFoodList.add(new PopularFood("Spaghetti with meat", "RM25.05", R.drawable.p3_speghettiwithmeatballs));
        popularFoodList.add(new PopularFood("Grilled Beef Steak", "RM7.05", R.drawable.p4_grilledsteak));
        popularFoodList.add(new PopularFood("Mexican Tacos", "RM17.05", R.drawable.p5_maxicantacos));

        setPopularRecycler(popularFoodList);


        List<AsiaFood> asiaFoodList = new ArrayList<>();
        asiaFoodList.add(new AsiaFood("Nasi Biryani", "RM20", R.drawable.af1_nasibriyani, "Briand Restaurant"));
        asiaFoodList.add(new AsiaFood("Kabsa Tandori Biryani", "RM25", R.drawable.af2_kabsatandooribriyani, "Friends Restaurant"));
        asiaFoodList.add(new AsiaFood("Rendang Thai", "RM20", R.drawable.af3_rendangthai, "Briand Restaurant"));
        asiaFoodList.add(new AsiaFood("Friend Chicken", "RM25", R.drawable.af5_friedchicken, "Friends Restaurant"));
        asiaFoodList.add(new AsiaFood("Fried Chicken", "RM20", R.drawable.af5_friedchicken, "Briand Restaurant"));
        setAsiaRecycler(asiaFoodList);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        return v;
    }

    private void setPopularRecycler(List<PopularFood> popularFoodList) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        popularRecycler.setLayoutManager(layoutManager);
        popularFoodAdapter = new PopularFoodAdapter(getContext(), popularFoodList);
        popularRecycler.setAdapter(popularFoodAdapter);

    }

    private void setAsiaRecycler(List<AsiaFood> asiaFoodList) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        asiaRecycler.setLayoutManager(layoutManager);
        asiaFoodAdapter = new AsiaFoodAdapter(getContext(), asiaFoodList);
        asiaRecycler.setAdapter(asiaFoodAdapter);

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

                            textView.setText("Hi, "+name+"\nFind your Favourite Food ");
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.baseline_store_mall_directory_grey).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.baseline_store_mall_directory_grey);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Error loading user info: " + error.getMessage());
                    }
                });
    }
}