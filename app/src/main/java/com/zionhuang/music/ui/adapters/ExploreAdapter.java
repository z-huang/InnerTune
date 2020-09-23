package com.zionhuang.music.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.youtube.model.Video;
import com.zionhuang.music.databinding.ItemVideoBinding;

public class ExploreAdapter extends PagingDataAdapter<Video, ExploreAdapter.ViewHolder> {

    public ExploreAdapter() {
        super(new ItemComparator());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemVideoBinding binding = ItemVideoBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemVideoBinding binding;

        public ViewHolder(ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Video video) {
            binding.setVideo(video);
        }
    }

    public Video getItemByPosition(int position) {
        return getItem(position);
    }

    static class ItemComparator extends DiffUtil.ItemCallback<Video> {
        @Override
        public boolean areItemsTheSame(@NonNull Video oldItem, @NonNull Video newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Video oldItem, @NonNull Video newItem) {
            return oldItem.getEtag().equals(newItem.getEtag());
        }
    }
}
