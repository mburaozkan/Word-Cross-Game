package com.hh.hs.wordsearch.commons

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addTo(disposables: CompositeDisposable) {
    disposables.add(this)
}