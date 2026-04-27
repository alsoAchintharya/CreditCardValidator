package com.example.cardvalidator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class CardListActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var adapter: CardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list)

        database = AppDatabase.getDatabase(this)
        adapter = CardAdapter(emptyList())

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCards)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                database.cardDao().getAllCards().collect { cards ->
                    adapter.updateCards(cards)
                }
            }
        }
    }
}