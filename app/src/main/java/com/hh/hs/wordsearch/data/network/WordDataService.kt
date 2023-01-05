package com.hh.hs.wordsearch.data.network

import com.hh.hs.wordsearch.data.network.responses.WordsUpdateResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface WordDataService {
    @GET("words")
    fun fetchWordsData(@Query("revision") currentRevision: Int): Observable<WordsUpdateResponse>
}