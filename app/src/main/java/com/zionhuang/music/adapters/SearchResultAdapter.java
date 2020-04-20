package com.zionhuang.music.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.zionhuang.music.R;
import com.zionhuang.music.Youtube.YtItem;
import com.zionhuang.music.adapters.selection.SelectableAdapter;
import com.zionhuang.music.adapters.selection.Selection;
import com.zionhuang.music.utils.NetworkManager;

import java.util.ArrayList;

public class SearchResultAdapter extends SelectableAdapter<String, SearchResultAdapter.ViewHolder> {
    private static final String TAG = "SearchResultAdapter";
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<YtItem.Base> mDataSet;
    private ImageLoader mImageLoader;
    private InterationListener mListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private TextView mDescription;
        private NetworkImageView mThumbnail;
        private AppCompatImageButton mActionButton;
        private View mSelectionOverlay;

        ViewHolder(View view, boolean isLoader) {
            super(view);
            if (isLoader) {
                return;
            }
            mTitle = view.findViewById(R.id.tv_item_title);
            mDescription = view.findViewById(R.id.tv_item_description);
            mThumbnail = view.findViewById(R.id.thumbnail_view);
            mActionButton = view.findViewById(R.id.button_more_action);
            mSelectionOverlay = view.findViewById(R.id.overlay_selection);

            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (!getSelection().hasSelection()) {
                    if (mListener != null) {
                        mListener.onItemClicked((YtItem.BaseItem) mDataSet.get(position));
                    }
                } else {
                    boolean selected = switchSelection(((YtItem.BaseItem) mDataSet.get(position)).getId());
                    mSelectionOverlay.setVisibility(selected ? View.VISIBLE : View.GONE);
                }

            });

            view.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return false;
                }
                YtItem.BaseItem item = (YtItem.BaseItem) mDataSet.get(position);
                boolean selected = switchSelection(item.getId());
                mSelectionOverlay.setVisibility(selected ? View.VISIBLE : View.GONE);
                return true;
            });

            mActionButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.getMenuInflater().inflate(R.menu.search_item, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.action_add:
                            Log.d(TAG, "clicked add");
                            break;
                        case R.id.action_download:
                            Log.d(TAG, "clicked download");
                            break;
                    }
                    return true;
                });
                popupMenu.show();
            });
        }

        void bindTo(YtItem.BaseItem item) {
            mTitle.setText(item.getTitle());
            mDescription.setText(item.getDescription());
            mThumbnail.setImageUrl(item.getThumbnailURL(), mImageLoader);
            mSelectionOverlay.setVisibility(isSelected(item.getId()) ? View.VISIBLE : View.GONE);
        }
    }

    public SearchResultAdapter(ArrayList<YtItem.Base> dataSet, Selection<String> selection, LifecycleOwner lifecycleOwner, Context context) {
        super(selection, lifecycleOwner);
        mContext = context;
        mDataSet = dataSet;
        mInflater = LayoutInflater.from(context);
        mImageLoader = NetworkManager.getInstance(context).getImageLoader();
        setHasStableIds(true);
    }

    public void setInteractionListener(InterationListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = mInflater.inflate(R.layout.item_video, parent, false);
            return new ViewHolder(v, false);
        } else {
            View v = mInflater.inflate(R.layout.item_loading, parent, false);
            return new ViewHolder(v, true);
        }
    }

    @Override
    protected String getKeyForPosition(int position) {
        YtItem.Base item = mDataSet.get(position);
        if (item instanceof YtItem.Loader) {
            return "Loader";
        }
        return ((YtItem.BaseItem) item).getId();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        YtItem.Base item = mDataSet.get(position);
        if (item instanceof YtItem.BaseItem) {
            holder.bindTo((YtItem.BaseItem) item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataSet.get(position) instanceof YtItem.BaseItem) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return mDataSet.get(position).hashCode();
    }

    public interface InterationListener {
        void onItemClicked(YtItem.BaseItem item);
    }
}
