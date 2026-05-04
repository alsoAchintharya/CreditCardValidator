package com.example.cardwallet.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AppDatabase
import data.CreditCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CardFormState(
    val cardNumber: String = "",
    val holderName: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val brandName: String? = null
)

class CardAddViewModel : ViewModel() {

    private lateinit var database: AppDatabase

    private val _formState = MutableStateFlow(CardFormState())
    val formState: StateFlow<CardFormState> = _formState

    private val _insertEvent = MutableStateFlow<InsertEvent?>(null)
    val insertEvent: StateFlow<InsertEvent?> = _insertEvent

    fun init(context: Context) {
        database = AppDatabase.getDatabase(context)
    }


    fun updateCardNumber(input: String, brand: String?) {
        _formState.value = _formState.value.copy(
            cardNumber = input,
            brandName = brand
        )
    }

    fun updateHolderName(name: String) {
        _formState.value = _formState.value.copy(
            holderName = name
        )
    }

    fun updateExpiry(expiry: String) {
        _formState.value = _formState.value.copy(
            expiry = expiry
        )
    }

    fun updateCvv(cvv: String) {
        _formState.value = _formState.value.copy(
            cvv = cvv
        )
    }

    fun insertCard(card: CreditCard) {
        viewModelScope.launch {
            try {
                database.cardDao().insert(card)
                _insertEvent.value = InsertEvent.Success
            } catch (e: Exception) {
                _insertEvent.value = InsertEvent.Error(e)
            }
        }
    }

    fun clearEvent() {
        _insertEvent.value = null
    }

    sealed class InsertEvent {
        object Success : InsertEvent()
        data class Error(val error: Exception) : InsertEvent()
    }
}