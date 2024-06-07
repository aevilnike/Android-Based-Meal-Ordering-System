package com.example.admin;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterSeller extends AppCompatActivity implements LocationListener{
    private ImageButton backBtn, gpsBtn;
    private ImageView profileIv;
    private EditText countryEt,stateEt,cityEt,addressEt;
    private TextInputLayout nameEt,emailEt,phoneEt,cpassEt,passEt,shopeNameEt;
    private Button registerBtn,registerUsertBtn;
    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //Image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permission arrays
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;
    //img picked uri
    private Uri image_uri;
    private double latitude=0.0, longitude=0.0;
    private LocationManager locationManager;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);
        //init ui views
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        emailEt = (TextInputLayout)findViewById(R.id.emailEt);
        nameEt = (TextInputLayout)findViewById(R.id.nameEt);
        phoneEt = (TextInputLayout)findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        passEt = (TextInputLayout)findViewById(R.id.passEt);
        cpassEt = (TextInputLayout)findViewById(R.id.cpassEt);
        shopeNameEt = (TextInputLayout)findViewById(R.id.shopeNameEt);
        registerBtn = findViewById(R.id.registerBtn);
        registerUsertBtn = findViewById(R.id.registerUsertBtn);

        //init permission array
        locationPermission = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick img
                //showImagePickDialog();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                someActivityResultLauncher.launch(intent);
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register user
                inputData();
            }
        });
        registerUsertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open register seller
                startActivity(new Intent(RegisterSeller.this, RegisterUser.class));
            }
        });
    }
    private String fullName, shopeName, phoneNumber, country, state, city, address, email, password, confirmPassword,phonenumformat;
    private void inputData() {
        //input data
        fullName = nameEt.getEditText().getText().toString().trim();
        shopeName = shopeNameEt.getEditText().getText().toString().trim();
        phoneNumber = phoneEt.getEditText().getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        email = emailEt.getEditText().getText().toString().trim();
        password = passEt.getEditText().getText().toString().trim();
        confirmPassword = cpassEt.getEditText().getText().toString().trim();
        // Validate data
        // Clear errors
        phonenumformat = "+6"+phoneNumber;
        clearErrors();
        if (TextUtils.isEmpty(shopeName)) {
            shopeNameEt.setError("Field cannot be empty");
            shopeNameEt.requestFocus(); // Set focus to this field
            return;
        }
        if (TextUtils.isEmpty(fullName)) {
            nameEt.setError("Field cannot be empty");
            nameEt.requestFocus(); // Set focus to this field
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneEt.setError("Mobile number is required");
            phoneEt.requestFocus(); // Set focus to this field
            return;
        } else {
            if (phoneNumber.length() < 10 || phoneNumber.length() > 11) {
                phoneEt.setError("Invalid mobile number");
                phoneEt.requestFocus(); // Set focus to this field
                return;
            }
        }
        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Please click GPS button to detect location...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Invalid Email Pattern");
            emailEt.requestFocus(); // Set focus to this field
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passEt.setError("Password is required");
            passEt.requestFocus(); // Set focus to this field
            return;
        } else {
            passEt.setError(null); // Clear the error
            if (!isPasswordValid(password)) {
                passEt.setErrorEnabled(true);
                passEt.setError("Password too weak");
                cpassEt.setError("password too weak");
            } else {
                passEt.setError(null);
                return;
            }
        }
        if (!password.equals(confirmPassword)) {
            cpassEt.setError("Password doesn't match");
            cpassEt.requestFocus(); // Set focus to this field
            return;
        }
        cpassEt.setError(null);
        createAccount();
    }
    private boolean isPasswordValid(String password) {
        // Password must have at least one uppercase letter
        // Password length must be between 6 and 12 characters
        // Password must have at least one symbol (you can modify the regex pattern)
        String regexPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!]).{6,12}$";

        return password.matches(regexPattern);
    }
    private void clearErrors() {
        // Clear errors for all TextInputLayouts
        nameEt.setError(null);
        shopeNameEt.setError(null);
        phoneEt.setError(null);
        emailEt.setError(null);
        passEt.setError(null);
        cpassEt.setError(null);
    }
    private void createAccount() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //account created
                saverFirebaseData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed creating account
                progressDialog.dismiss();
                Toast.makeText(RegisterSeller.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void saverFirebaseData() {
        progressDialog.setMessage("Saving Account Info...");
        String timestamp = ""+System.currentTimeMillis();

        if(image_uri==null){
            //save info without image
            //setup data to save
            HashMap<String, Object> haspMap = new HashMap<>();
            haspMap.put("uid",""+firebaseAuth.getUid());
            haspMap.put("email",""+email);
            haspMap.put("name",""+fullName);
            haspMap.put("shopName",""+shopeName);
            haspMap.put("phone",""+phonenumformat);
            haspMap.put("country",""+country);
            haspMap.put("state",""+state);
            haspMap.put("city",""+city);
            haspMap.put("address",""+address);
            haspMap.put("latitude",""+latitude);
            haspMap.put("longitude",""+longitude);
            haspMap.put("timestamp",""+timestamp);
            haspMap.put("accountType","Seller");
            haspMap.put("online","true");
            haspMap.put("shopOpen","true");
            haspMap.put("profileImage","");

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(haspMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //db updated
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSeller.this, MainAdminActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed updating db
                            progressDialog.dismiss();
                            Toast.makeText(RegisterSeller.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                haspMap.put("uid",""+firebaseAuth.getUid());
                                haspMap.put("email",""+email);
                                haspMap.put("name",""+fullName);
                                haspMap.put("shopName",""+shopeName);
                                haspMap.put("phone",""+phonenumformat);
                                haspMap.put("country",""+country);
                                haspMap.put("state",""+state);
                                haspMap.put("city",""+city);
                                haspMap.put("address",""+address);
                                haspMap.put("latitude",""+latitude);
                                haspMap.put("longitude",""+longitude);
                                haspMap.put("timestamp",""+timestamp);
                                haspMap.put("accountType","Seller");
                                haspMap.put("online","true");
                                haspMap.put("shopOpen","true");
                                haspMap.put("profileImage",""+downlaodImageUri);//url upload image

                                //save to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(haspMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //db updated
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSeller.this, MainAdminActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed updating db
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSeller.this, MainAdminActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterSeller.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
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
    private void showImagePickDialog() {
        //option to display in dialog
        String[] options = {"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle clicks
                if (which == 0) {
                    //camera clicked
                    if (checkCameraPermission()) {
                        //camera permission allowed
                        pickFromCamera();
                    } else {
                        //now allowed, request
                        requestCameraPermission();
                    }
                } else {
                    //gallery clicked
                    if (checkStoragePermission()) {
                        //storage permission allowed
                        pickFromGallery();
                    } else {
                        //now allowed, request
                        requestStoragePermission();
                    }
                }
            }
        }).show();
    }
    public void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }
    public void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private  void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    private  void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }
    @Override
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
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length<0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Camera permission are necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            } break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length<0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage permission are necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            } break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //get picked image
                image_uri = data.getData();
                //set to imageview
                profileIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageview
                profileIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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
                        Toast.makeText(RegisterSeller.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            });
}