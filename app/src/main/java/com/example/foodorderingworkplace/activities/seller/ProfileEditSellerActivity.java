package com.example.foodorderingworkplace.activities.seller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingworkplace.R;
import com.example.foodorderingworkplace.activities.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditSellerActivity extends AppCompatActivity implements LocationListener {

    private ImageButton backBtn,gpsBtn;
    private ImageView profileIv;
    private EditText shopeNameEt,nameEt,phoneEt,countryEt,stateEt,cityEt,addressEt;
    private SwitchCompat shopOpenSwitch;
    private TextView full_name,shop_name,orderCountTv,payoutTv,totalIncomeTv,payoutViewTv;
    private Button updateBtn;
    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    //permission array
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;
    //img picked uri
    private Uri image_uri;
    private double latitude=0.0, longitude=0.0, amount=0.0;
    private LocationManager locationManager;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_seller);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        shopeNameEt = findViewById(R.id.shopeNameEt);
        updateBtn = findViewById(R.id.updateBtn);
        shopOpenSwitch = findViewById(R.id.shopOpenSwitch);
        full_name = findViewById(R.id.full_name);
        shop_name = findViewById(R.id.shop_name);
        orderCountTv = findViewById(R.id.orderCountTv);
        payoutTv = findViewById(R.id.payoutTv);
        totalIncomeTv = findViewById(R.id.totalIncomeTv);
        payoutViewTv = findViewById(R.id.payoutViewTv);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();

        //init permission array
        locationPermission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        payoutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePayoutRequest(amount);
            }
        });
        payoutViewTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileEditSellerActivity.this, PayoutSeller.class);
                startActivity(intent);
            }
        });
        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
                if (checkLocationPermission()) {
                    detectLocation();
                } else {
                    requestLocationPermission();
                }
            }
        });
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick img
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                someActivityResultLauncher.launch(intent);
            }
        });
    }

    private void initiatePayoutRequest(double amount) {
        DatabaseReference payoutRef = FirebaseDatabase.getInstance().getReference("PayoutRequests");

        // Create a unique key for the payout request
        String payoutRequestId = payoutRef.push().getKey();

        // Create a HashMap with payout request details
        HashMap<String, Object> payoutMap = new HashMap<>();
        payoutMap.put("payoutId", payoutRequestId);
        payoutMap.put("sellerUid", firebaseAuth.getUid()); // Add seller's UID
        payoutMap.put("amount", amount);
        payoutMap.put("status", "pending");
        payoutMap.put("timestamp", System.currentTimeMillis()); // Use current time as timestamp

        // Set the payout request in the database
        payoutRef.child(payoutRequestId).setValue(payoutMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Payout request successfully added
                        Toast.makeText(ProfileEditSellerActivity.this, "Payout request submitted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileEditSellerActivity.this, PayoutSeller.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add payout request
                        Toast.makeText(ProfileEditSellerActivity.this, "Failed to submit payout request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //update profile
    private String fullName, shopeName, phoneNumber, country, state, city, address;
    boolean shopOpen;
    private void inputData() {
        //input data
        fullName = nameEt.getText().toString().trim();
        shopeName = shopeNameEt.getText().toString().trim();
        phoneNumber = phoneEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        shopOpen = shopOpenSwitch.isChecked();

        updateProfile();
    }
    String phonenumformat;
    private void updateProfile() {
        progressDialog.setMessage("Updating Info...");
        progressDialog.show();
        phonenumformat = "+6"+phoneNumber;
        if(image_uri==null){
            //save info without image
            //setup data to save
            HashMap<String, Object> haspMap = new HashMap<>();
            haspMap.put("name",""+fullName);
            haspMap.put("shopName",""+shopeName);
            haspMap.put("phone",""+phonenumformat);
            haspMap.put("country",""+country);
            haspMap.put("state",""+state);
            haspMap.put("city",""+city);
            haspMap.put("address",""+address);
            haspMap.put("latitude",""+latitude);
            haspMap.put("longitude",""+longitude);
            haspMap.put("shopOpen",""+shopOpen);

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).updateChildren(haspMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //db updated
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed updating db
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //save with image
            //name and path of image
            String filePathAndNAme = "profile_images/" + ""+firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndNAme);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downlaodImageUri = uriTask.getResult();

                            if(uriTask.isSuccessful()){
                                //setup data to save
                                HashMap<String, Object> haspMap = new HashMap<>();
                                haspMap.put("name",""+fullName);
                                haspMap.put("shopName",""+shopeName);
                                haspMap.put("phone",""+phonenumformat);
                                haspMap.put("country",""+country);
                                haspMap.put("state",""+state);
                                haspMap.put("city",""+city);
                                haspMap.put("address",""+address);
                                haspMap.put("latitude",""+latitude);
                                haspMap.put("longitude",""+longitude);
                                haspMap.put("shopOpen",""+shopOpen);
                                haspMap.put("profileImage",""+downlaodImageUri);//url upload image

                                //save to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).updateChildren(haspMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //db updated
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed updating db
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //getCurrent user data
    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String name = ""+ds.child("name").getValue();
                            String shopname = ""+ds.child("shopName").getValue();
                            // Assuming phone is in the format "+6XXXXXXXXXX"
                            String phone = ""+ds.child("phone").getValue();
                            String country = ""+ds.child("country").getValue();
                            String state = ""+ds.child("state").getValue();
                            String city = ""+ds.child("city").getValue();
                            String address = ""+ds.child("address").getValue();
                            latitude = Double.parseDouble(""+ds.child("latitude").getValue());
                            longitude = Double.parseDouble(""+ds.child("longitude").getValue());
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String online = ""+ds.child("online").getValue();
                            amount = ds.child("payout").getValue(Double.class);
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String shopOpen = ""+ds.child("shopOpen").getValue();
                            String uid = ""+ds.child("uid").getValue();
                            long orderCount = ds.child("Orders").getChildrenCount();
                            // Check if the seller has an existing payout request
                            DatabaseReference payoutRequestsRef = FirebaseDatabase.getInstance().getReference("PayoutRequests");
                            payoutRequestsRef.orderByChild("sellerUid").equalTo(firebaseAuth.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                // Seller has an existing payout request
                                                payoutTv.setVisibility(View.GONE);
                                                payoutViewTv.setVisibility(View.VISIBLE);
                                            } else {
                                                // Seller does not have an existing payout request
                                                payoutTv.setVisibility(View.VISIBLE);
                                                payoutViewTv.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle error
                                        }
                                    });

                            // Extract digits after the country code
                            String phoneNumberWithoutCountryCode = phone.substring(2);
                            totalIncomeTv.setText(String.format(Locale.getDefault(), "RM%.2f", amount));
                            orderCountTv.setText(String.valueOf(orderCount));
                            full_name.setText(name);
                            shop_name.setText(shopname);
                            nameEt.setText(name);
                            shopeNameEt.setText(shopname);
                            phoneEt.setText(phoneNumberWithoutCountryCode);
                            countryEt.setText(country);
                            stateEt.setText(state);
                            cityEt.setText(city);
                            addressEt.setText(address);

                            if(shopOpen.equals("true")){
                                shopOpenSwitch.setChecked(true);
                            } else {
                                shopOpenSwitch.setChecked(false);
                            }

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
    //Camera method
    private final ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        image_uri = result.getData().getData();
                        //set to imageview
                        profileIv.setImageURI(image_uri);
                    } else {
                        Toast.makeText(ProfileEditSellerActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    //Get location method
    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,this);
    }
    private void findAddress() {
        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);//complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set address
            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            addressEt.setText(address);

        }  catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        findAddress();
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length<0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        //permission allowed
                        detectLocation();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Location permission is necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            } break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //LocationListener.super.onProviderDisabled(provider);
        Toast.makeText(this, "Please turn on location...", Toast.LENGTH_SHORT).show();
    }
}