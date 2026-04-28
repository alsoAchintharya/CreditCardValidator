package com.example.cardwallet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cardwallet.adapters.CardAdapter
import com.example.cardwallet.databinding.ActivityCardListBinding
import data.AppDatabase
import kotlinx.coroutines.launch

class CardListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardListBinding

    private lateinit var database: AppDatabase
    private lateinit var adapter: CardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        database = AppDatabase.getDatabase(this)
        adapter = CardAdapter(emptyList())

        val recyclerView = binding.recyclerViewCards
        val addButton = binding.addcard


        addButton.setOnClickListener {
            startActivity(Intent(this, CardAddActivity::class.java))
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