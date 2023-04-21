package com.example.acneapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Classification> classifications;
    private OnDeleteClickListener onDeleteClickListener;


    public HistoryAdapter(List<Classification> classifications, OnDeleteClickListener onDeleteClickListener) {
        this.classifications = classifications;
        this.onDeleteClickListener = onDeleteClickListener;
    }


    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
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
        holder.date.setText(classification.getTimestamp());
        holder.result.setText(classification.getResult());

        // 여드름 종류에 따라 적절한 문자열을 사용합니다.
        String resultText = classification.getResult();
        if (resultText.contains("acne_comedonia")) {
            resultText = resultText.replace("acne_comedonia", "면포성 여드름");
        }
        else if (resultText.contains("acne_papules")) {
            resultText = resultText.replace("acne_papules", "구진성 여드름");
        }
        else if (resultText.contains("acne_pustular")) {
            resultText = resultText.replace("acne_pustular", "농포성 여드름");
        }
        // 필요한 경우 다른 여드름 종류에 대한 변경도 여기에 추가하세요.

        holder.result.setText(resultText);
    }


    @Override
    public int getItemCount() {
        return classifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView imageName;
        TextView result;
        TextView resultTextView;
        TextView timestampTextView;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            resultTextView = itemView.findViewById(R.id.result_textview);
            timestampTextView = itemView.findViewById(R.id.timestamp_textview);
            deleteButton = itemView.findViewById(R.id.delete_button);
            date = itemView.findViewById(R.id.timestamp_textview); // XML 레이아웃에서 TextView의 ID와 일치하도록 설정해주세요.
            result = itemView.findViewById(R.id.result_textview); // XML 레이아웃에서 TextView의 ID와 일치하도록 설정해주세요.
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Classification classification);
    }
}

