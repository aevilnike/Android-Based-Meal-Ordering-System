package com.example.foodorderingworkplace.activities.seller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
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

public class EditFoodActivity extends AppCompatActivity {
    private String foodId;
    //uiviews
    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt,description,categoryEt,priceEt;
    private TextView d;
    private Button updateBtn;
    private Uri image_uri;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private SwitchCompat foodOpenSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food);

        backBtn = findViewById(R.id.backBtn);
        updateBtn = findViewById(R.id.updateBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        description = findViewById(R.id.description);
        categoryEt = findViewById(R.id.categoryEt);
        foodOpenSwitch = findViewById(R.id.foodOpenSwitch);
        priceEt = findViewById(R.id.priceEt);

        //get id of the product from intent
        foodId = getIntent().getStringExtra("foodId");

        //setup progress dialog
        firebaseAuth = FirebaseAuth.getInstance();
        loadFoodDetails();//to set food details on view

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
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }
    private String foodName,desc,category,quantity,price;
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
        updateFood();
    }
    private void loadFoodDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Foods").child(foodId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String foodId = ""+snapshot.child("foodId").getValue();
                        String foodName = ""+snapshot.child("foodName").getValue();
                        String foodDescription = ""+snapshot.child("foodDescription").getValue();
                        String foodCategory = ""+snapshot.child("foodCategory").getValue();
                        String foodAvailability = ""+snapshot.child("foodAvailability").getValue();
                        String foodIcon = ""+snapshot.child("foodIcon").getValue();
                        String price = ""+snapshot.child("price").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        //set data
                        titleEt.setText(foodName);
                        description.setText(foodDescription);
                        categoryEt.setText(foodCategory);
                        if (foodAvailability.equals("true")) {
                            foodOpenSwitch.setChecked(true);
                        } else {
                            foodOpenSwitch.setChecked(false);
                        }
                        priceEt.setText(price);

                        try {
                            Picasso.get().load(foodIcon).placeholder(R.drawable.baseline_no_food_white).into(productIconIv);
                        } catch (Exception e) {
                            productIconIv.setImageResource(R.drawable.baseline_no_food_white);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void updateFood() {
        progressDialog.setMessage("Updating food..");
        progressDialog.show();

        if(image_uri == null){
            //update without image
            //setup data in hashmap to update
            HashMap<String, Object> haspMap = new HashMap<>();
            haspMap.put("foodName",""+foodName);
            haspMap.put("foodDescription",""+desc);
            haspMap.put("foodCategory",""+category);
            haspMap.put("foodAvailability",""+foodOpenSwitch.isChecked());
            haspMap.put("price",""+price);

            //update to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Foods").child(foodId)
                    .updateChildren(haspMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //update success
                            progressDialog.dismiss();
                            Toast.makeText(EditFoodActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //update failed
                            progressDialog.dismiss();
                            Toast.makeText(EditFoodActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //upload with image
            //first upload image
            //image name and path on firebase storage
            String filePathAndName = "food_images/" + "" + foodId;//override previous image using the same id
            Log.d("EditFoodActivity", "Food ID: " + filePathAndName);
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded image
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadImageUri) {
                                    //setup data in hashmap to update
                                    HashMap<String, Object> haspMap = new HashMap<>();
                                    haspMap.put("foodName", "" + foodName);
                                    haspMap.put("foodDescription", "" + desc);
                                    haspMap.put("foodCategory", "" + category);
                                    haspMap.put("foodAvailability", "" + foodOpenSwitch.isChecked());
                                    haspMap.put("price", "" + price);
                                    haspMap.put("foodIcon", "" + downloadImageUri);//url upload image
                                    Log.d("EditFoodActivity", "Image Upload Success. Download URL: " + downloadImageUri.toString());
                                    //save to db
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                    ref.child(firebaseAuth.getUid()).child("Foods").child(foodId)
                                            .updateChildren(haspMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    //db updated
                                                    progressDialog.dismiss();
                                                    Toast.makeText(EditFoodActivity.this, "Food Updated...", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    //failed updating db
                                                    progressDialog.dismiss();
                                                    Toast.makeText(EditFoodActivity.this, "DB Updated Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    Log.e("EditFoodActivity", "DB Update Failed", e);
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditFoodActivity.this, "Image Upload Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("EditFoodActivity", "Image Upload Failed", e);
                        }
                    });
        }
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
                        Toast.makeText(EditFoodActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            });
}