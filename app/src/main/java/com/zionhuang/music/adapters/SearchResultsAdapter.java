package com.zionhuang.music.adapters;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.NetworkImageView;
import com.zionhuang.music.utils.NetworkManager;
import com.zionhuang.music.utils.Player;
import com.zionhuang.music.R;
import com.zionhuang.music.utils.Youtube;
import com.zionhuang.music.viewmodels.MainViewModel;

import java.util.ArrayList;

public class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private MainViewModel mainViewModel;
    private LayoutInflater mInflater;
    private Player mPlayer;
    private ArrayList<Youtube.Item.Base> mDataset;

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title_tv;
        TextView description_tv;
        NetworkImageView thumbnail;

        ItemViewHolder(View v) {
            super(v);
            title_tv = v.findViewById(R.id.title_text_view);
            description_tv = v.findViewById(R.id.description_text_view);
            thumbnail = v.findViewById(R.id.thumbnail_view);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Log.d("ResultFragment", "Element " + position + " clicked.");
                    if (mDataset.get(position) instanceof Youtube.Item.Video) {
                        Youtube.Item.Video item = (Youtube.Item.Video) mDataset.get(position);
                        mainViewModel.getCurrentSong().setValue(new Pair<String, String>(item.getTitle(), item.getChannelTitle()));
                        mPlayer.loadItem(item, mPlayer, mContext);
                    }

                }
            });
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(View v) {
            super(v);
        }
    }

    public SearchResultsAdapter(ArrayList<Youtube.Item.Base> dataSet, Context context, MainViewModel mainViewModel) {
        mContext = context;
        this.mainViewModel = mainViewModel;
        mDataset = dataSet;
        mInflater = LayoutInflater.from(context);
        mPlayer = Player.getInstance(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = mInflater.inflate(R.layout.item_video, parent, false);
            return new ItemViewHolder(v);
        } else {
            View v = mInflater.inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Youtube.Item.Base item = mDataset.get(position);
        if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).title_tv.setText(((Youtube.Item.ItemBase) item).getTitle());
            ((ItemViewHolder) holder).description_tv.setText(((Youtube.Item.ItemBase) item).getDescription());
            ((ItemViewHolder) holder).thumbnail.setImageUrl(((Youtube.Item.ItemBase) item).getThumbnailURL(), NetworkManager.getInstance().getImageLoader());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataset.get(position) instanceof Youtube.Item.ItemBase) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

