package com.example.admin;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PayoutRequestAdapter extends RecyclerView.Adapter<PayoutRequestAdapter.HolderPayout>{

    private Context context;
    private ArrayList<PayoutRequestModel> payoutRequestList;
    private OnItemClickListener listener;

    public PayoutRequestAdapter(Context context, ArrayList<PayoutRequestModel> payoutRequestList) {
        this.context = context;
        this.payoutRequestList = payoutRequestList;
    }

    public interface OnItemClickListener {
        void onAcceptClick(int position);
        void onRejectClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HolderPayout onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_seller, parent, false);
        return new HolderPayout(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPayout holder, int position) {
        PayoutRequestModel modelPayout = payoutRequestList.get(position);
        //get data
        final String payoutUid = modelPayout.getPayoutId();
        String sellerUid = modelPayout.getSellerUid();
        String status = modelPayout.getStatus();
        long timestamp = modelPayout.getTimestamp();
        double amount = modelPayout.getAmount();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(Long.parseLong(String.valueOf(timestamp))));
        //set data
        holder.statusTv.setText(status);
        holder.amountTv.setText(String.format(Locale.getDefault(), "RM%.2f", amount));
        holder.dateTv.setText(formattedDate);
        // Set seller's name
        holder.nameTv.setText(modelPayout.getSellerName());

        // Load and set seller's profile image using Picasso or Glide
        Picasso.get().load(modelPayout.getSellerProfileImage()).placeholder(R.drawable.baseline_store_mall_directory_grey).into(holder.profileIv);
        // Set click listeners for buttons
        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAcceptClick(position);
                }
            }
        });

        holder.rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRejectClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return payoutRequestList.size();
    }

    class HolderPayout extends RecyclerView.ViewHolder {

        ImageView profileIv;
        TextView nameTv,dateTv,amountTv,acceptBtn,rejectBtn,statusTv;

        public HolderPayout(@NonNull View itemView) {
            super(itemView);

            //init gui
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
