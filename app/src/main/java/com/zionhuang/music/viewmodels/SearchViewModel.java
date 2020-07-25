package com.zionhuang.music.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.zionhuang.music.youtube.YoutubeRepository;
import com.zionhuang.music.youtube.YtResult;
import com.zionhuang.music.adapters.selection.Selection;
import com.zionhuang.music.adapters.selection.SimpleKeyStorage;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private static final String TAG = "SearchResultVM";
    private YoutubeRepository mYoutubeRepo;

    private final SimpleKeyStorage<String> mKeyStorage = new SimpleKeyStorage<>();
    private final Selection<String> mSelection = new Selection<>(mKeyStorage);

    public SearchViewModel(@NonNull Application application) {
        super(application);
        mYoutubeRepo = YoutubeRepository.getInstance(application);
    }

    public LiveData<List<String>> fetchSuggestions(String query) {
        return mYoutubeRepo.fetchSuggestions(query);
    }

    public LiveData<YtResult> search(String query, String pageToken) {
        return mYoutubeRepo.search(query, pageToken);
    }

    public Selection<String> getSelection() {
        return mSelection;
    }
}
