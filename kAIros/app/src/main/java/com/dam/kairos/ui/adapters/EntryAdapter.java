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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dam.kairos.R;
import com.dam.kairos.data.model.Entry;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    private List<Entry> entryList;
    private final Context context;

    public EntryAdapter(Context context, List<Entry> entryList) {
        this.context = context;
        this.entryList = entryList;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        Entry entry = entryList.get(position);

        holder.textView.setText(entry.getText());

        // Mostrar imagen si existe
        if (entry.getImageUrl() != null && !entry.getImageUrl().isEmpty()) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(context).load(entry.getImageUrl()).into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

        // Configuración del botón de "like"
        holder.likeButton.setImageResource(entry.isLiked() ? R.drawable.entry_feed_robotichand_filled : R.drawable.entry_feed_robotichand);
        holder.likeButton.setOnClickListener(v -> {
            boolean liked = !entry.isLiked();
            entry.setLiked(liked);
            holder.likeButton.setImageResource(liked ? R.drawable.entry_feed_robotichand_filled : R.drawable.entry_feed_robotichand);
            // Aquí podrías actualizar Firestore si es necesario
        });

        // Botón de guardar (solo ejemplo visual)
        holder.saveButton.setOnClickListener(v -> {
            Toast.makeText(context, "Guardado (no implementado aún)", Toast.LENGTH_SHORT).show();
        });

        // Botón de comentar
        holder.commentButton.setOnClickListener(v -> {
            holder.commentInput.setVisibility(View.VISIBLE);
            holder.commentInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(holder.commentInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Enviar comentario (si quieres hacer algo con él)
        holder.commentInput.setOnEditorActionListener((v, actionId, event) -> {
            String comment = v.getText().toString().trim();
            if (!comment.isEmpty()) {
                // Aquí podrías guardar el comentario en Firestore
                Toast.makeText(context, "Comentario enviado", Toast.LENGTH_SHORT).show();
                v.setText("");
                v.setVisibility(View.GONE);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public void setEntries(List<Entry> entries) {
        this.entryList = entries;
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        ImageButton likeButton, saveButton, commentButton;
        EditText commentInput;

        public EntryViewHolder(@NonNull View itemView) {
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

