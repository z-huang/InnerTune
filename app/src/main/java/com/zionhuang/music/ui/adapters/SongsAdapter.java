package com.zionhuang.music.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zionhuang.music.databinding.ItemSongBinding;
import com.zionhuang.music.db.SongEntity;

import java.util.Collections;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    private List<SongEntity> mDataSet = Collections.emptyList();

    public SongsAdapter() {
    }

    public void setDataSet(List<SongEntity> list) {
        mDataSet = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSongBinding viewBinding = ItemSongBinding.inflate(inflater, parent, false);
        return new ViewHolder(viewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public SongEntity getSongFromPosition(int i) {
        return mDataSet.get(i);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemSongBinding binding;

        public ViewHolder(ItemSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SongEntity song) {
            binding.setSong(song);
            binding.executePendingBindings();
        }
    }
}
