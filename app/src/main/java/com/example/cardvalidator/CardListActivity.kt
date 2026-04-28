package com.example.cardvalidator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cardvalidator.adapters.CardAdapter
import data.AppDatabase
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

        val addButton = findViewById<Button>(R.id.addcard)
        val profile_return_btn = findViewById<Button>(R.id.back_profile)

        addButton.setOnClickListener {
            startActivity(Intent(this, CardAddActivity::class.java))
        }

        profile_return_btn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }


        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        val userId = intent.getLongExtra("userId", -1L)

        lifecycleScope.launch {
            database.cardDao().getCardsForUser(userId)
                .collect { cards ->
                    adapter.updateCards(cards)
                }
        }
    }
}