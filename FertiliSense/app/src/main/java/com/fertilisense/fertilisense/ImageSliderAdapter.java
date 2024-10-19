package com.fertilisense.fertilisense;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder> {

    private final Context context;
    private final int[] images;
    private final String[] descriptions;
    private final String[] urls;

    // Constructor
    public ImageSliderAdapter(Context context, int[] images, String[] descriptions, String[] urls) {
        this.context = context;
        this.images = images;
        this.descriptions = descriptions;
        this.urls = urls;
    }


    @NonNull
    @Override
    public ImageSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.slider_item, parent, false);
        return new ImageSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSliderViewHolder holder, int position) {
        holder.imageView.setImageResource(images[position]);
        holder.description.setText(descriptions[position]);

        // Set click listener to open the URL associated with the image
        holder.itemView.setOnClickListener(v -> {
            String url = urls[position];
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class ImageSliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView description;

        ImageSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSliderItem);
            description = itemView.findViewById(R.id.imageDescription);
        }
    }
}
