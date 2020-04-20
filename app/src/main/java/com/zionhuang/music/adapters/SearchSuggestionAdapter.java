package com.zionhuang.music.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zionhuang.music.R;
import com.zionhuang.music.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {
    private List<String> mDataSet;
    private MainViewModel mMainViewModel;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private MainViewModel mMainViewModel;
        TextView textView;

        ViewHolder(View view, MainViewModel mainViewModel) {
            super(view);
            mMainViewModel = mainViewModel;
            textView = view.findViewById(R.id.textView);

            view.setOnClickListener(v -> mMainViewModel.setSearchQuery(textView.getText().toString(), true));
            view.findViewById(R.id.fill_text_button).setOnClickListener(v -> mMainViewModel.setSearchQuery(textView.getText().toString(), false));
        }
    }

    public SearchSuggestionAdapter(ArrayList<String> dataSet, MainViewModel mainViewModel) {
        mDataSet = dataSet;
        mMainViewModel = mainViewModel;
    }

    public void setData(List<String> data) {
        mDataSet = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);

        return new ViewHolder(v, mMainViewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(mDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void clear() {
        int size = getItemCount();
        mDataSet.clear();
        notifyItemRangeRemoved(0, size);
    }
}

