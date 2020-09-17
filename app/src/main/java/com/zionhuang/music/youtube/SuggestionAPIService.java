package com.zionhuang.music.youtube;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SuggestionAPIService {
    @GET("/complete/search?client=firefox&ds=yt")
    Observable<SuggestionResult> suggest(@Query("q") String query);
}
