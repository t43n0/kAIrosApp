package com.dam.kairos.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.kairos.R;
import com.dam.kairos.data.model.Entry;
import com.dam.kairos.ui.adapters.FeedAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerView;
    private FeedAdapter adapter;
    private List<Entry> entryList = new ArrayList<Entry>();
    private FirebaseFirestore db;
    private ImageView progressBar;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        // Inicialización
        recyclerView = view.findViewById(R.id.recyclerViewEntries);
        progressBar = view.findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeedAdapter(getContext(), entryList);
        recyclerView.setAdapter(adapter);

        // Cargar entradas del feed
        loadFeedPosts();

        return view;
    }

    private void loadFeedPosts() {
        FirebaseFirestore.getInstance().collection("entries")
                .whereEqualTo("isPublic", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Entry> posts = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Entry entry = doc.toObject(Entry.class);
                        posts.add(entry);
                    }
                    adapter = new FeedAdapter(getContext(), posts);
                    recyclerView.setAdapter(adapter);
                });
    }
}
