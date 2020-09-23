package com.zionhuang.music.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.youtube.model.SearchResult;
import com.zionhuang.music.databinding.ItemSearchResultBinding;

import java.util.Objects;

public class SearchResultAdapter extends PagingDataAdapter<SearchResult, SearchResultAdapter.ViewHolder> {
    private static final String TAG = "SearchResultAdapter";

    public SearchResultAdapter() {
        super(new ItemComparator());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchResultBinding videoBinding = ItemSearchResultBinding.inflate(inflater, parent, false);
        return new ViewHolder(videoBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        switch (Objects.requireNonNull(getItem(position)).getId().getKind()) {
            case "youtube#video":
                return 0;
            case "youtube#channel":
                return 1;
            case "youtube#playlist":
                return 2;
        }
        return -1;
    }

    public SearchResult getItemByPosition(int position) {
        return getItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemSearchResultBinding binding;

        public ViewHolder(ItemSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SearchResult item) {
            binding.setItem(item);
        }
    }

    static class ItemComparator extends DiffUtil.ItemCallback<SearchResult> {
        @Override
        public boolean areItemsTheSame(@NonNull SearchResult oldItem, @NonNull SearchResult newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SearchResult oldItem, @NonNull SearchResult newItem) {
            return oldItem.getEtag().equals(newItem.getEtag());
        }
    }
}
