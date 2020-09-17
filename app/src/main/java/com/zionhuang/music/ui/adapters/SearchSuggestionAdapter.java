package com.zionhuang.music.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zionhuang.music.databinding.ItemSuggestionBinding;
import com.zionhuang.music.viewmodels.YouTubeSuggestionViewModel;

import java.util.Collections;
import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {
    private List<String> mDataSet = Collections.emptyList();
    private YouTubeSuggestionViewModel mViewModel;

    public SearchSuggestionAdapter(YouTubeSuggestionViewModel viewModel) {
        mViewModel = viewModel;
    }

    public void setDataSet(List<String> dataSet) {
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSuggestionBinding binding = ItemSuggestionBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public String getQueryByPosition(int position) {
        return mDataSet.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemSuggestionBinding binding;

        ViewHolder(ItemSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String query) {
            binding.setQuery(query);
            binding.executePendingBindings();
            binding.fillTextButton.setOnClickListener(v -> mViewModel.setQuery(binding.getQuery()));
        }
    }
}

