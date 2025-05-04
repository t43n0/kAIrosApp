package com.dam.kairos.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dam.kairos.R;
import com.dam.kairos.data.model.EntryDiario;

import java.util.List;

public class EntryDiarioAdapter extends RecyclerView.Adapter<EntryDiarioAdapter.EntryViewHolder> {

    private List<EntryDiario> entryList;
    private final Context context;

    public EntryDiarioAdapter(Context context, List<EntryDiario> entryList) {
        this.context = context;
        this.entryList = entryList;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.entry_item, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        EntryDiario entryDiario = entryList.get(position);

        holder.textView.setText(entryDiario.getText());

        // Mostrar imagen si existe
        if (entryDiario.getImageUrl() == null) {
            Glide.with(context)
                    .asGif()
                    .load(R.drawable.loading_gif) // loading.gif en drawable
                    .into(holder.imageView);
        } else  if (entryDiario.getImageUrl().isEmpty()){
            holder.imageView.setVisibility(View.GONE);
        } else {
            holder.imageView.setVisibility(View.VISIBLE);
            String baseUrl = "https://10.0.2.2:3001";
            Glide.with(context)
                    .load(baseUrl + entryDiario.getImageUrl())
                    .apply(new RequestOptions().error(R.drawable.entry_feed_error))
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public void setEntries(List<EntryDiario> entries) {
        this.entryList = entries;
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvContent);
            imageView = itemView.findViewById(R.id.image);
        }
    }
}

