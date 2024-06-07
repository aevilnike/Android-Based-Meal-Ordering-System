package com.example.foodorderingworkplace.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingworkplace.FilterFood;
import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.activities.seller.EditFoodActivity;
import com.example.foodorderingworkplace.models.ModelFood;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterFoodSeller extends RecyclerView.Adapter<AdapterFoodSeller.HolderFoodSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelFood> foodList, filterList;
    private FilterFood filter;

    public AdapterFoodSeller(Context context, ArrayList<ModelFood> foodList) {
        this.context = context;
        this.foodList = foodList;
        this.filterList = foodList;
    }

    @NonNull
    @Override
    public HolderFoodSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_food_seller, parent, false);
        return new HolderFoodSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderFoodSeller holder, int position) {
        //get data
        ModelFood modelFood = foodList.get(position);
        String fid = modelFood.getFoodId();
        String fuid = modelFood.getUid();
        String fname = modelFood.getFoodName();
        String fcategory = modelFood.getFoodCategory();
        String fdescription = modelFood.getFoodDescription();
        String ficon = modelFood.getFoodIcon();
        String fAvailability = modelFood.getfoodAvailability();
        String fprice = modelFood.getPrice();
        String ftimestamp = modelFood.getTimestamp();

        //set Food Availability
        if (fAvailability.equals("false")) {
            holder.availableTv.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
            holder.availableTv.setText("Unavailable");
            holder.availableTv.setBackgroundResource(R.drawable.shape_rectred);
            // Disable click for items with quantity 0
            //holder.itemView.setClickable(false);
            //holder.itemView.setEnabled(false);
        }
        //set data
        holder.foodNameTv.setText(fname);
        holder.priceTv.setText("RM"+fprice);

        try {
            Picasso.get().load(ficon).placeholder(R.drawable.baseline_add_shopping_cart_primary).into(holder.productIconIv);
        } catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.baseline_add_shopping_cart_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle item clicks, show item details
                detailsBottomSheet(modelFood);//here modelFood contains details of clicked product
            }
        });
    }

    private void detailsBottomSheet(ModelFood modelFood) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view for bottomsheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_food_details_seller, null);
        //set view to bottomsheet
        bottomSheetDialog.setContentView(view);

        //init views of bottomsheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView foodNameTv = view.findViewById(R.id.foodNameTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView priceTv = view.findViewById(R.id.priceTv);
        TextView availableTv = view.findViewById(R.id.availableTv);
        //get data
        String fid = modelFood.getFoodId();
        String fuid = modelFood.getUid();
        String fname = modelFood.getFoodName();
        String fcategory = modelFood.getFoodCategory();
        String fdescription = modelFood.getFoodDescription();
        String ficon = modelFood.getFoodIcon();
        String fAvailability = modelFood.getfoodAvailability();
        String fprice = modelFood.getPrice();
        String ftimestamp = modelFood.getTimestamp();
        //set data
        foodNameTv.setText(fname);
        priceTv.setText("RM"+fprice);
        categoryTv.setText(fcategory);
        descriptionTv.setText(fdescription);
        //set Food Availability
        if (fAvailability.equals("false")) {
            availableTv.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
            availableTv.setText("Unavailable");
            availableTv.setBackgroundResource(R.drawable.shape_rectred);
            // Disable click for items with quantity 0
            //holder.itemView.setClickable(false);
            //holder.itemView.setEnabled(false);
        }
        //set image
        try {
            Picasso.get().load(ficon).placeholder(R.drawable.baseline_add_shopping_cart_primary).into(productIconIv);
        } catch (Exception e) {
            productIconIv.setImageResource(R.drawable.baseline_add_shopping_cart_primary);
        }

        //show dialog
        bottomSheetDialog.show();

        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //open edit food activity, pass id of food
                Intent intent = new Intent(context, EditFoodActivity.class);
                intent.putExtra("foodId", fid);
                context.startActivity(intent);
            }
        });
        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure want to delete food "+fname+"?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteFood(fid);//fid is the food id
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel, dismiss dialog
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        //back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void deleteFood(String fid) {
        //delete food using its id
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Foods").child(fid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //food deleted
                        Toast.makeText(context, "Food deleted...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting product
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterFood(this, filterList);
        }
        return filter;
    }

    class HolderFoodSeller extends RecyclerView.ViewHolder {
        /*hold views of recycle view*/
        private ImageView productIconIv,nextIv;
        private TextView availableTv,foodNameTv,priceTv;
        public HolderFoodSeller(@NonNull View itemView) {
            super(itemView);

            productIconIv = itemView.findViewById(R.id.productIconIv);
            nextIv = itemView.findViewById(R.id.nextIv);
            availableTv = itemView.findViewById(R.id.availableTv);
            foodNameTv = itemView.findViewById(R.id.foodNameTv);
            priceTv = itemView.findViewById(R.id.priceTv);

        }
    }
}
