package com.hh.hs.wordsearch.data.network.responses

import com.google.gson.annotations.SerializedName

class WordsUpdateResponse {
    @SerializedName("update")
    var isUpdate = false

    @SerializedName("revision")
    var revision = 0

    @SerializedName("data")
    var data: List<ThemeResponse>? = null
}