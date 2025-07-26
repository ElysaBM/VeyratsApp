package com.example.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class UnknownPersonAdapter extends RecyclerView.Adapter<UnknownPersonAdapter.ViewHolder> {

    // Define an interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(UnknownPerson person);
    }

    private Context context;
    private List<UnknownPerson> unknownPersonList;
    private OnItemClickListener listener;

    // Constructor now includes listener
    public UnknownPersonAdapter(Context context, List<UnknownPerson> unknownPersonList, OnItemClickListener listener) {
        this.context = context;
        this.unknownPersonList = unknownPersonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_unknown_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UnknownPerson person = unknownPersonList.get(position);

        // Set the timestamp
        holder.timestamp.setText(person.getTimestamp());

        // Load the image using Glide
        Glide.with(context)
                .load(person.getImageUrl())
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideDebug", "Error loading image: " + (e != null ? e.getMessage() : "null"));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d("GlideDebug", "Image loaded successfully.");
                        return false;
                    }
                })
                .into(holder.imageView);

        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(person);
            }
        });
    }

    @Override
    public int getItemCount() {
        return unknownPersonList.size();
    }

    // ViewHolder to hold the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }

    // Optional: for external data updates
    public void updateData(List<UnknownPerson> newPersonList) {
        unknownPersonList.clear();
        unknownPersonList.addAll(newPersonList);
        notifyDataSetChanged();
    }
}
