package com.example.acneapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Classification> classifications;
    private OnDeleteClickListener onDeleteClickListener;

    public HistoryAdapter(List<Classification> classifications, OnDeleteClickListener onDeleteClickListener) {
        this.classifications = classifications;
        this.onDeleteClickListener = onDeleteClickListener;
    }
    // In your HistoryAdapter class
    public void setClassifications(List<Classification> classifications) {
        this.classifications = classifications;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_classification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Classification classification = classifications.get(position);
        holder.resultTextView.setText(classification.getResultStr());
        holder.timestampTextView.setText(classification.getTimeStamp());

        // Set onDeleteClickListener to the delete button
        holder.deleteButton.setOnClickListener(view -> onDeleteClickListener.onDeleteClick(classification));
    }

    @Override
    public int getItemCount() {
        return classifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView resultTextView;
        TextView timestampTextView;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            resultTextView = itemView.findViewById(R.id.result_textview);
            timestampTextView = itemView.findViewById(R.id.timestamp_textview);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Classification classification);
    }
}

