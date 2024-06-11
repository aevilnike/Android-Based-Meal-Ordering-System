package com.example.foodorderingworkplace.activities.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodorderingworkplace.Constants;
import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.activities.ShopReviewsActivity;
import com.example.foodorderingworkplace.adapters.AdapterCartItem;
import com.example.foodorderingworkplace.adapters.AdapterFoodUser;
import com.example.foodorderingworkplace.adapters.AdapterReview;
import com.example.foodorderingworkplace.models.ModelCartItem;
import com.example.foodorderingworkplace.models.ModelFood;
import com.example.foodorderingworkplace.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity implements PaymentResultListener {
    //declare ui views
    private TextView shopNameTv,emailTv,phoneTv,filteredFoodTv,addressTv,openCloseTv,cartCountTv;
    private EditText searchFoodEt;
    private ImageButton cartBtn,backBtn,filterFoodBtn,mapBtn,callBtn,reviewsBtn;
    private ImageView shopIv;
    private RecyclerView foodsRv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String shopUid,shopName,shopEmail,shopPhone,shopAddress,myLatitude,myLongitude,shopLatitude,shopLongitude;
    private String myName,myEmail,myPhone,myOrderId;
    private ArrayList<ModelFood> foodsList;
    private AdapterFoodUser adapterFoodUser;
    //Cart
    private ArrayList<ModelCartItem> cartItemsList;
    private AdapterCartItem adapterCartItem;
    private RatingBar ratingBar;
    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterReview;
    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init ui views
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        filteredFoodTv = findViewById(R.id.filteredFoodTv);
        addressTv = findViewById(R.id.addressTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        filterFoodBtn = findViewById(R.id.filterFoodBtn);
        mapBtn = findViewById(R.id.mapBtn);
        callBtn = findViewById(R.id.callBtn);
        searchFoodEt = findViewById(R.id.searchFoodEt);
        shopIv = findViewById(R.id.shopIv);
        foodsRv = findViewById(R.id.foodsRv);
        cartCountTv = findViewById(R.id.cartCountTv);
        reviewsBtn = findViewById(R.id.reviewsBtn);
        ratingBar = findViewById(R.id.ratingBar);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get uid of the shop from intent
        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopFoods();
        loadReviews();//avg rating set on ratingbar

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();
        //each shop have its own foods, so if user add items to cart and go back and open cart in different shop then cart should be different
        //so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM Token", "Token: " + token);
                    } else {
                        Log.e("FCM Token", "Failed to get token: " + task.getException());
                    }
                });
        //search
        searchFoodEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterFoodUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCartDialog();
            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        filterFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.foodoptions1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.foodoptions1[which];
                                filteredFoodTv.setText(selected);
                                if (selected.equals("All")) {
                                    //load all
                                    loadShopFoods();
                                } else {
                                    adapterFoodUser.getFilter().filter(selected);
                                }
                            }
                        }).show();
            }
        });
        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass shop uid to ShopReviewActivity
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
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

                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue()); // exp 4.3
                            ratingSum = ratingSum + rating; //for avg rating, add all ratings then will divide it by number of reviews

                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();
    }

    //make it public can access in adapter
    public void cartCount(){
        //keep it public
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            //no item in cart, hide cart count textview
            cartCountTv.setVisibility(View.GONE);
        }
        else {
            //have items in cart, show cart count textview and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);
        }
    }
    //need to access therese views in adapter so making public
    public double allTotalPrice = 0.00;
    public TextView allTotalPriceTv;
    private void showCartDialog() {
        //init list
        cartItemsList = new ArrayList<>();
        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        TextView checkOutBtn = view.findViewById(R.id.checkOutBtn);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String totalprice = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(totalprice);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+totalprice,
                    ""+quantity
            );
            //setup adapter
            cartItemsList.add(modelCartItem);
        }
        // Set the adapter and notify changes outside the loop
        adapterCartItem = new AdapterCartItem(this, cartItemsList);
        // set to recyclerview
        cartItemsRv.setAdapter(adapterCartItem);
        allTotalPriceTv.setText("RM" + String.format("%.2f", allTotalPrice));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.0;
            }
        });

        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartItemsList .size() == 0) {
                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
                    return; //dont proceed further
                }
                //for order id and order time
                final String timestamp = ""+System.currentTimeMillis();
                myOrderId = timestamp;

                System.out.println("Payment user email: " + myEmail);
                Log.d("TAG", "Payment user email: " + myEmail);
                System.out.println("Payment user phone: " + myPhone);
                Log.d("TAG", "Payment user phone: " + myPhone);
                String desc = "Order id:"+myOrderId+" Name:"+myName+" Email:"+myEmail+" Phone:"+myPhone;

                progressDialog.dismiss();
                paymentMethod(allTotalPrice,myName,myEmail,myEmail,desc);
            }
        });
    }

    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        //for order id and order time
        //final String timestamp = ""+System.currentTimeMillis();
        final String timestamp = myOrderId;
        String cost = allTotalPriceTv.getText().toString().trim().replace("RM","");//remove RM if contains
        //Create payout value
        // Calculate payout value based on the total cost
        double payoutValue = calculatePayoutValue(allTotalPrice);

        // Update the seller's account with the payout value
        updateSellerPayout(payoutValue);
        // Update the seller's account with the payout value
        Log.e("TAG", "payout value: " + payoutValue);
        updateSellerPayout(payoutValue);
        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "In Progress");//In Progress/Completed/Cancelled
        hashMap.put("orderCost", ""+cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);

        //add latitude, longitude of user to each order | delete previous orders from firebase or add manually to them
        //payment gateway
        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items
                        for (int i=0; i<cartItemsList.size(); i++) {
                            String pId = cartItemsList.get(i).getfId();
                            String id = cartItemsList.get(i).getId();
                            String cost = cartItemsList.get(i).getCost();
                            String name = cartItemsList.get(i).getName();
                            String price = cartItemsList.get(i).getPrice();
                            String quantity = cartItemsList.get(i).getQuantity();
                            
                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("cost", cost);
                            hashMap1.put("name", name);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);
                            
                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private double calculatePayoutValue(double totalCost) {
        // For example, you might decide to give sellers 80% of the total cost
        double payoutPercentage = 0.8;
        return totalCost * payoutPercentage;
    }

    private void updateSellerPayout(double payoutValue) {
        // Get reference to the seller's account in the database
        DatabaseReference sellerRef = FirebaseDatabase.getInstance().getReference("Users").child(shopUid);

        // Retrieve the current payout value from the database
        sellerRef.child("payout").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Update the payout value by adding the new payout
                double currentPayout = snapshot.exists() ? snapshot.getValue(Double.class) : 0.0;
                double newPayout = currentPayout + payoutValue;
                sellerRef.child("payout").setValue(newPayout);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                Toast.makeText(ShopDetailsActivity.this, "Failed to update payout: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void paymentMethod(double amount, String name, String phone, String email, String myOrderId) {
        Checkout checkout = new Checkout();
        checkout.setKeyID("");
        final Activity activity = ShopDetailsActivity.this;

        try {
            JSONObject options = new JSONObject();

            // Convert the amount to the closest integer value, and ensure it meets the minimum value of 10 sen
            int amountInSen = (int) Math.max(amount * 100, 10);

            //Set Company Name
            options.put("name", ""+name);
            //Ref no
            options.put("description", ""+myOrderId);
            //Image to be display
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            //options.put("order_id", "order_DBJOWzybf0sJbb");
            options.put("theme.color", "#3399cc");
            // Currency type
            options.put("currency", "MYR");
            options.put("amount", amountInSen);
            options.put("prefill.email", ""+email);
            options.put("prefill.contact",""+phone);
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e("TAG", "Error in starting Razorpay Checkout", e);
        }

    }
    private void openMap() {
        //saddr = source address
        //daddr = destination address
        /*String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);*/
        String uri = "http://maps.google.com/maps?daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            myName = ""+ds.child("name").getValue();
                            myEmail = ""+ds.child("email").getValue();
                            myPhone = ""+ds.child("phone").getValue();
                            String employeeId = ""+ds.child("employeeId").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Error loading user info: " + error.getMessage());
                    }
                });
    }
    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get shop data
                            String name = ""+snapshot.child("name").getValue();
                            shopName = ""+snapshot.child("shopName").getValue();
                            shopPhone = ""+snapshot.child("phone").getValue();
                            shopEmail = ""+snapshot.child("email").getValue();
                            shopAddress = ""+snapshot.child("address").getValue();;
                            String profileImage = ""+snapshot.child("profileImage").getValue();
                            String shopOpen = ""+snapshot.child("shopOpen").getValue();
                            shopLatitude = ""+snapshot.child("latitude").getValue();
                            shopLongitude = ""+snapshot.child("longitude").getValue();
                            //set data
                            shopNameTv.setText(shopName);
                            phoneTv.setText(shopPhone);
                            emailTv.setText(shopEmail);
                            addressTv.setText(shopAddress);
                            if (shopOpen.equals("true")) {
                                openCloseTv.setText("Open");
                            } else {
                                openCloseTv.setText("Closed");
                            }
                            try {
                                Picasso.get().load(profileImage).into(shopIv);
                            } catch (Exception e) {
                                shopIv.setImageResource(R.drawable.baseline_store_mall_directory_grey);
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Error loading shop info: " + error.getMessage());
                    }
                });
    }
    private void loadShopFoods() {
        //init list
        foodsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Foods")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        foodsList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelFood modelFood = ds.getValue(ModelFood.class);
                            foodsList.add(modelFood);
                        }
                        //setup adapter
                        adapterFoodUser = new AdapterFoodUser(ShopDetailsActivity.this, foodsList);
                        //set adapter
                        foodsRv.setAdapter(adapterFoodUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void prepareNotificationMessage(String orderId){
        //when user places order, send notification to seller
        //prepare data for notification
        Log.e("FCM Notification Error", "Error: " + orderId);
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid()); //since we are logged in as buyer to place order to so current user uid is buyer uid
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderUid", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);//to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);
        } catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        Log.e("FCM Notification Error", "Error: " + orderId + notificationJo);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start order details activity
                //after placing order open order details page
                //open order details, we need to keys there, orderId, orderTo
                Log.e("FCM Notification Error", "Error: " + response + notificationJo);
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("FCM Notification Error", "Error: " + error.getMessage());
                //if failed sending fcm, still start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key=" + Constants.FCM_KEY);
                return headers;
            }
        };
        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
        submitOrder();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.e("PaymentError", "Error code: " + i + ", Message: " + s);
        Toast.makeText(this, "Payment Cancel", Toast.LENGTH_SHORT).show();
    }
}
