package com.example.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;


public class LoginTabFragment extends Fragment {


    EditText usernameEt,passEt;
    Button loginBtn;
    private Uri image_uri;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    float v = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.login_tab_fragment, container, false);

        usernameEt = root.findViewById(R.id.usernameEt);
        passEt = root.findViewById(R.id.passEt);
        loginBtn = root.findViewById(R.id.loginBtn);

        usernameEt.setTranslationX(800);
        passEt.setTranslationX(800);
        loginBtn.setTranslationX(800);

        usernameEt.setAlpha(v);
        passEt.setAlpha(v);
        loginBtn.setAlpha(v);

        usernameEt.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        passEt.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        loginBtn.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        return root;
    }
    private void checkUserType() {
        //if user is seller, start seller main
        //if user is buyer, start user main screen

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            if (accountType.equals("Admin")) {
                                progressDialog.dismiss();
                                //user is seller
                                startActivity(new Intent(getActivity(), MainAdminActivity.class));
                            } else {
                                progressDialog.dismiss();
                                //user is buyer
                                startActivity(new Intent(getActivity(), MainAdminActivity.class));
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private String username, password;
    private void loginUser() {
        username = usernameEt.getText().toString().trim();
        password = passEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            Toast.makeText(getActivity(), "Invalid email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6 || password.length() > 12) {
            Toast.makeText(getActivity(), "Required Password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(username, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //logged in successfully
                        makeMeOnline();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            // Handle case where the account doesn't exist
                            Toast.makeText(getActivity(), "Account does not exist", Toast.LENGTH_SHORT).show();
                        } else {
                            // Other login failure, show a generic message
                            Toast.makeText(getActivity(), "Failed to log in: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void makeMeOnline() {
        //after logging in, make user online
        progressDialog.setMessage("Checking User...");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","true");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    /*private String username, password;
    private void inputData() {
        //input data
        username = usernameEt.getText().toString().trim();
        password = passEt.getText().toString().trim();

        createAccount();
    }*/
    /*private void createAccount() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(username, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
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
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/
    /*private void saverFirebaseData() {
        progressDialog.setMessage("Saving Account Info...");
        String timestamp = ""+System.currentTimeMillis();

        if(image_uri==null){
            //save info without image
            //setup data to save
            HashMap<String, Object> haspMap = new HashMap<>();
            haspMap.put("uid",""+firebaseAuth.getUid());
            haspMap.put("username",""+username);
            haspMap.put("password",""+password);
            haspMap.put("timestamp",""+timestamp);
            haspMap.put("accountType","Admin");

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(haspMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //db updated
                            progressDialog.dismiss();
                            startActivity(new Intent(getActivity(), MainAdminActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed updating db
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }*/
}