package com.example.cardwallet.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AppDatabase
import data.CreditCard
import kotlinx.coroutines.launch

class CardAddViewModel : ViewModel() {

    private lateinit var database: AppDatabase

    fun init(context: Context) {
        database = AppDatabase.getDatabase(context)
    }

    fun insertCard(card: CreditCard, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                database.cardDao().insert(card)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
