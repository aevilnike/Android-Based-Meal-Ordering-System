package com.example.foodorderingworkplace.activities.seller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderingworkplace.Constants;
import com.example.foodorderingworkplace.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddFoodActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt,description,categoryEt,priceEt;
    private SwitchCompat foodOpenSwitch;
    private TextView d;
    private Button addFoodBtn;
    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    //permission array
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;
    //img picked uri
    private Uri image_uri;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        backBtn = findViewById(R.id.backBtn);
        addFoodBtn = findViewById(R.id.addFoodBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        description = findViewById(R.id.description);
        categoryEt = findViewById(R.id.categoryEt);
        priceEt = findViewById(R.id.priceEt);
        foodOpenSwitch = findViewById(R.id.foodOpenSwitch);

        cameraPermission = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //setup progress dialog
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
        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick img
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                someActivityResultLauncher.launch(intent);
            }
        });
        categoryEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick category
                categoryDialog();
            }
        });
        addFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow:
                //1. Input data
                //2. Validate data
                //3. Add data to db
                inputData();
            }
        });
    }

    private String foodName,desc,category,price;
    private boolean foodAvailability;
    private void inputData() {
        //1. Input data
        foodName = titleEt.getText().toString().trim();
        desc = description.getText().toString().trim();
        category = categoryEt.getText().toString().trim();
        foodAvailability = foodOpenSwitch.isChecked();
        price = priceEt.getText().toString().trim();
        //2. velidate data
        if (TextUtils.isEmpty(foodName)) {
            Toast.makeText(this, "Food Name required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            Toast.makeText(this, "Description required...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Select your food category...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Insert the price...", Toast.LENGTH_SHORT).show();
            return;
        }
        addProduct();
    }

    private void addProduct() {
        //3. Add data to db
        progressDialog.setMessage("Adding New Food...");
        progressDialog.show();
        String timestamp = ""+System.currentTimeMillis();

        if (image_uri == null) {
            //upload without image
            //setup data to upload
            HashMap<String, Object> haspMap = new HashMap<>();
            haspMap.put("foodId",""+timestamp);
            haspMap.put("foodName",""+foodName);
            haspMap.put("foodDescription",""+desc);
            haspMap.put("foodCategory",""+category);
            haspMap.put("foodAvailability",""+foodAvailability);
            haspMap.put("foodIcon",""+"");//no img, set empty
            haspMap.put("price",""+price);
            haspMap.put("timestamp",""+timestamp);
            haspMap.put("uid",""+firebaseAuth.getUid());
            //add to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Foods").child(timestamp).setValue(haspMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //added to db
                            progressDialog.dismiss();
                            Toast.makeText(AddFoodActivity.this, "Foods added...", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding to db
                            progressDialog.dismiss();
                            Toast.makeText(AddFoodActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            //upload with img
            //first upload image to storage
            //name and path of image to be uploaded
            String filePathAndName = "food_images/" + "" + timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded
                            //get url uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                //url of image received
                                //upload without image
                                //setup data to upload
                                HashMap<String, Object> haspMap = new HashMap<>();
                                haspMap.put("foodId",""+timestamp);
                                haspMap.put("foodName",""+foodName);
                                haspMap.put("foodDescription",""+desc);
                                haspMap.put("foodCategory",""+category);
                                haspMap.put("foodAvailability",""+foodAvailability);
                                haspMap.put("foodIcon",""+downloadImageUri);
                                haspMap.put("price",""+price);
                                haspMap.put("timestamp",""+timestamp);
                                haspMap.put("uid",""+firebaseAuth.getUid());
                                //add to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Foods").child(timestamp).setValue(haspMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //added to db
                                                progressDialog.dismiss();
                                                Toast.makeText(AddFoodActivity.this, "Foods added...", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding to db
                                                progressDialog.dismiss();
                                                Toast.makeText(AddFoodActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            progressDialog.dismiss();
                            Toast.makeText(AddFoodActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearData() {
        //clear data after uploading product
        titleEt.setText("");
        description.setText("");
        categoryEt.setText("");
        foodOpenSwitch.setChecked(false);
        priceEt.setText("");
        productIconIv.setImageResource(R.drawable.baseline_no_food_primary);
        image_uri = null;
    }

    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Food Category").setItems(Constants.foodoptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get picked category
                String category = Constants.foodoptions[which];
                //set picked category
                categoryEt.setText(category);
            }
        }).show();
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
                        productIconIv.setImageURI(image_uri);
                    } else {
                        Toast.makeText(AddFoodActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            });
}