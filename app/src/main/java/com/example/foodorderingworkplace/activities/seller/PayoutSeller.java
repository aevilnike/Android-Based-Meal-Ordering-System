package com.example.foodorderingworkplace.activities.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.foodorderingworkplace.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PayoutSeller extends AppCompatActivity {
    TextView amountTv,statusTv,dateTv;
    ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout_seller);
        amountTv = findViewById(R.id.amountTv);
        statusTv = findViewById(R.id.statusTv);
        backBtn = findViewById(R.id.backBtn);
        dateTv = findViewById(R.id.dateTv);

        loadPayoutRequests();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadPayoutRequests() {
        DatabaseReference payoutRef = FirebaseDatabase.getInstance().getReference("PayoutRequests");

        payoutRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the existing data or adapter
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String sellerUid = ds.child("sellerUid").getValue(String.class);
                    double amount = ds.child("amount").getValue(Double.class);
                    String status = ds.child("status").getValue(String.class);
                    long timestamp = ds.child("timestamp").getValue(Long.class);
                    // Convert timestamp to Date
                    Date date = new Date(timestamp);

                    // Format the Date to a string using SimpleDateFormat
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy \nHH:mm a", Locale.getDefault());
                    String fordate = sdf.format(date);
                    // Now you have sellerUid, amount, status, and timestamp for each payout request
                    // Add your logic to display or handle these details
                    amountTv.setText(String.format(Locale.getDefault(), "RM%.2f", amount));
                    statusTv.setText(status);
                    dateTv.setText(fordate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

}