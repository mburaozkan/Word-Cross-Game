package com.hh.hs.wordsearch.features.settings

import android.os.Bundle
import android.view.MenuItem
import com.hh.hs.wordsearch.R

/**
 * Created by abdularis on 21/07/17.
 */
class SettingsActivity : AppCompatPreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.prefs_main)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.settings)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}