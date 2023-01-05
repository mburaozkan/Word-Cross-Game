package com.hh.hs.wordsearch.features.gamehistory

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hh.hs.wordsearch.R
import com.hh.hs.wordsearch.WordSearchApp
import com.hh.hs.wordsearch.commons.goneIf
import com.hh.hs.wordsearch.custom.easyadapter.MultiTypeAdapter
import com.hh.hs.wordsearch.features.FullscreenActivity
import com.hh.hs.wordsearch.features.gameplay.GamePlayActivity
import com.hh.hs.wordsearch.model.GameDataInfo
import kotlinx.android.synthetic.main.activity_game_history.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameHistoryActivity : FullscreenActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: GameHistoryViewModel by viewModels { viewModelFactory }
    private val adapter: MultiTypeAdapter = MultiTypeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_history)
        (application as WordSearchApp).appComponent.inject(this)

        initRecyclerView()
        viewModel.onGameDataInfoLoaded.observe(this) { gameDataInfoList: List<GameDataInfo> ->
            onGameDataInfoLoaded(gameDataInfoList)
        }
        btnClear.setOnClickListener {
            lifecycleScope.launch { viewModel.clear() }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.loadGameHistory()
        }
    }

    private fun onGameDataInfoLoaded(gameDataInfoList: List<GameDataInfo>) {
        textEmpty.goneIf(gameDataInfoList.isNotEmpty())
        adapter.setItems(gameDataInfoList)
    }

    private fun initRecyclerView() {
        val gameDataAdapterDelegate = GameDataAdapterDelegate()
        gameDataAdapterDelegate.onClickListener = object : GameDataAdapterDelegate.OnClickListener {
            override fun onClick(gameDataInfo: GameDataInfo?) {
                val intent = Intent(this@GameHistoryActivity, GamePlayActivity::class.java)
                intent.putExtra(GamePlayActivity.EXTRA_GAME_DATA_ID, gameDataInfo!!.id)
                startActivity(intent)
            }

            override fun onDeleteClick(gameDataInfo: GameDataInfo?) {
                gameDataInfo?.let { deleteGameData(gameDataInfo) }
            }
        }

        adapter.addDelegate(gameDataAdapterDelegate)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun deleteGameData(gameDataInfo: GameDataInfo) {
        lifecycleScope.launch {
            viewModel.deleteGameData(gameDataInfo.id)
        }
    }
}