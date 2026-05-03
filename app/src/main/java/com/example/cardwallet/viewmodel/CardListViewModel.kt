package com.example.cardwallet.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AppDatabase
import data.CreditCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CardListViewModel : ViewModel() {

    private lateinit var database: AppDatabase

    private val _cards = MutableStateFlow<List<CreditCard>>(emptyList())
    val cards: StateFlow<List<CreditCard>> = _cards

    fun init(context: Context) {
        database = AppDatabase.getDatabase(context)
    }

    fun loadCards(userId: Long) {
        viewModelScope.launch {
            database.cardDao().getCardsForUser(userId).collect { cardList ->
                _cards.value = cardList
            }
        }
    }
}
