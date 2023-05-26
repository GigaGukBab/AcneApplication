package com.example.acneapplication.BookMarkFunc;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acneapplication.R;

import java.util.ArrayList;

public class DermatologyAdapter extends RecyclerView.Adapter<DermatologyAdapter.DermatologyViewHolder> {
    private ArrayList<Dermatology> dermatologyList;

    public DermatologyAdapter(ArrayList<Dermatology> dermatologyList) {
        this.dermatologyList = dermatologyList;
    }

    @NonNull
    @Override
    public DermatologyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dermatology_item, parent, false);
        return new DermatologyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DermatologyAdapter.DermatologyViewHolder holder, int position) {
        Dermatology dermatology = dermatologyList.get(position);
        holder.bind(dermatology);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nmap://search?query=" + dermatology.getName() + "&appname=com.example.acneapplication"));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dermatologyList.size();
    }

    public static class DermatologyViewHolder extends RecyclerView.ViewHolder {
        TextView dermatologyName;

        public DermatologyViewHolder(@NonNull View itemView) {
            super(itemView);
            dermatologyName = itemView.findViewById(R.id.dermatology_name);
        }

        public void bind(Dermatology dermatology) {
            dermatologyName.setText(dermatology.getName());
        }
    }


}
