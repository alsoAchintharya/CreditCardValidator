package com.example.cardwallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cardwallet.adapters.CardAdapter
import com.example.cardwallet.databinding.ActivityCardListBinding
import com.example.cardwallet.viewmodel.CardListViewModel
import kotlinx.coroutines.launch

class CardListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardListBinding
    private lateinit var adapter: CardAdapter
    private val viewModel: CardListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityBase.logoutBtn.setOnClickListener {
            val intent = Intent(this, LogActivity::class.java)
            //viewModel.clearUser()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        binding.activityBase.backProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        viewModel.init(applicationContext)
        adapter = CardAdapter(emptyList())

        val recyclerView = binding.recyclerViewCards
        val addButton = binding.addcard
        val userId = intent.getLongExtra("userId", -1L)

        val snapHelper = androidx.recyclerview.widget.LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        if (userId == -1L) {
            finish()
            return
        }

        addButton.setOnClickListener {
            val intent = Intent(this, CardAddActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        viewModel.loadCards(userId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cards.collect { cards ->
                    adapter.updateCards(cards)
                }
            }
        }
    }
}
