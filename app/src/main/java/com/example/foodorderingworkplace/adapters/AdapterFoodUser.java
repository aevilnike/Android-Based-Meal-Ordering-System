package com.example.foodorderingworkplace.adapters;

import android.app.Activity;
import android.content.Context;
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

import com.example.foodorderingworkplace.FilterFoodUser;
import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.activities.user.ShopDetailsActivity;
import com.example.foodorderingworkplace.models.ModelFood;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterFoodUser extends RecyclerView.Adapter<AdapterFoodUser.HolderFoodUser> implements Filterable {

    private Context context;
    public ArrayList<ModelFood> foodsList, filterList;
    private FilterFoodUser filter;
    public AdapterFoodUser(Context context, ArrayList<ModelFood> foodsList) {
        this.context = context;
        this.foodsList = foodsList;
        this.filterList = foodsList;
    }

    @NonNull
    @Override
    public HolderFoodUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_food_user,parent,false);
        return new HolderFoodUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderFoodUser holder, int position) {
        //get data
        ModelFood modelFood = foodsList.get(position);
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
            holder.itemView.setClickable(false);
            holder.itemView.setEnabled(false);
            holder.addToCartTv.setClickable(false);
            holder.addToCartTv.setEnabled(false);
        }
        //set data
        holder.foodNameTv.setText(fname);
        holder.priceTv.setText("RM"+fprice);
        holder.descriptionTv.setText(fdescription);

        try {
            Picasso.get().load(ficon).placeholder(R.drawable.baseline_add_shopping_cart_primary).into(holder.productIconIv);
        } catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.baseline_add_shopping_cart_primary);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add food to cart
                showQuantityDialog(modelFood);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show food details

            }
        });
    }
    //Use For show quantity dialog after click food
    private double price = 0.0;
    private double totalPrice = 0.0;
    private int quantity = 0;
    private void showQuantityDialog(ModelFood modelFood) {
        //inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);
        //init layout view
        ImageView foodIv = view.findViewById(R.id.foodIv);
        TextView foodNameTv = view.findViewById(R.id.foodNameTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView priceTv = view.findViewById(R.id.priceTv);
        TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        ImageView continueBtn = view.findViewById(R.id.continueBtn);
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        //get data from model
        String foodId = modelFood.getFoodId();
        String foodName = modelFood.getFoodName();
        String foodDescription = modelFood.getFoodDescription();
        String foodPrice = modelFood.getPrice();
        String foodImage = modelFood.getFoodIcon();

        price = Double.parseDouble(foodPrice.replaceAll("RM",""));
        totalPrice = Double.parseDouble(foodPrice.replaceAll("RM",""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        //set data
        try {
            Picasso.get().load(foodImage).placeholder(R.drawable.baseline_add_shopping_cart_white).into(foodIv);
        } catch (Exception e) {
            foodIv.setImageResource(R.drawable.baseline_add_shopping_cart_white);
        }
        foodNameTv.setText(""+foodName);
        quantityTv.setText(""+quantity);
        descriptionTv.setText(""+foodDescription);
        priceTv.setText("RM"+totalPrice);
        finalPriceTv.setText("RM"+foodPrice);

        AlertDialog dialog = builder.create();
        dialog.show();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //increment quantity of the foods
        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalPrice = totalPrice + price;
                quantity++;

                finalPriceTv.setText("RM"+totalPrice);
                quantityTv.setText(""+quantity);
            }
        });
        //decrement quantity of the foods
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quantity>1){
                    totalPrice = totalPrice - price;
                    quantity--;

                    finalPriceTv.setText("RM"+totalPrice);
                    quantityTv.setText(""+quantity);
                }
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = foodName;
                String priceEach = String.format("%.2f", price);
                String totalprice = finalPriceTv.getText().toString().trim().replace("RM", "");
                String q = quantityTv.getText().toString().trim();

                //add to db(SQLite)
                addToCart(foodId, name, priceEach, totalprice, q);
                dialog.dismiss();
            }
        });
    }
    private int itemId = 1;
    private void addToCart(String foodId, String name, String priceEach, String totalprice, String quantity) {
        itemId++;

        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();
        
        Boolean b = easyDB.addData("Item_id",itemId)
                .addData("Item_PID", foodId)
                .addData("Item_Name", name)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", totalprice)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to cart...", Toast.LENGTH_SHORT).show();
        //Update cart count
        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return foodsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterFoodUser(this, filterList);
        }
        return filter;
    }

    class HolderFoodUser extends RecyclerView.ViewHolder {
        //uid views
        private ImageView productIconIv;
        private TextView availableTv,foodNameTv,descriptionTv,quantityTv,addToCartTv,priceTv;
        public HolderFoodUser(@NonNull View itemView) {
            super(itemView);
            productIconIv = itemView.findViewById(R.id.productIconIv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            availableTv = itemView.findViewById(R.id.availableTv);
            foodNameTv = itemView.findViewById(R.id.foodNameTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            priceTv = itemView.findViewById(R.id.priceTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
        }
    }
}
