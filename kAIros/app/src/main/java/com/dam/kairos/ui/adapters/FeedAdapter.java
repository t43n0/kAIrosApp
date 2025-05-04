package com.dam.kairos.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dam.kairos.R;
import com.dam.kairos.data.model.Entry;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private List<Entry> entryList;
    private Context context;

    public FeedAdapter(Context context, List<Entry> entryList) {
        this.context = context;
        this.entryList = entryList;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Entry entry = entryList.get(position);

        // Muestra el texto de la entrada
        holder.textView.setText(entry.getText());

        // Verifica si hay una URL de imagen en la entrada y la carga con Glide
        String imageUrl = entry.getImageUrl();  // Asumiendo que tienes un campo `imageUrl` en tu modelo Entry
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.loading_gif)  // Una imagen placeholder mientras se carga
                    .error(R.drawable.entry_feed_error)  // Una imagen para errores
                    .into(holder.imageView);  // Carga la imagen en el ImageView
            holder.imageView.setVisibility(View.VISIBLE);  // Asegúrate de que el ImageView esté visible si hay imagen
        } else {
            holder.imageView.setVisibility(View.GONE);  // Si no hay imagen, oculta el ImageView
        }

        // Like
        if (entry.isLiked()) {
            holder.likeButton.setImageResource(R.drawable.entry_feed_robotichand_filled);
        } else {
            holder.likeButton.setImageResource(R.drawable.entry_feed_robotichand);
        }

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cambiar el estado de "like" y actualizar el botón
                boolean liked = !entry.isLiked();
                entry.setLiked(liked);
                holder.likeButton.setImageResource(liked ? R.drawable.entry_feed_robotichand_filled : R.drawable.entry_feed_robotichand);
                // TODO: Actualizar el conteo de likes en Firestore
            }
        });

        // Save
        holder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implementar lógica de guardado
            }
        });

        // Comentario
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.commentInput.setVisibility(View.VISIBLE);
                holder.commentInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(holder.commentInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        ImageButton likeButton, saveButton, commentButton;
        EditText commentInput;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textFeedContent);
            imageView = itemView.findViewById(R.id.imageFeed);
            likeButton = itemView.findViewById(R.id.buttonLike);
            saveButton = itemView.findViewById(R.id.buttonSave);
            commentButton = itemView.findViewById(R.id.buttonComment);
            commentInput = itemView.findViewById(R.id.editTextComment);
        }
    }
}
