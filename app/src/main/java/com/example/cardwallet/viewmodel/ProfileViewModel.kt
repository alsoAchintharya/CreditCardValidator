package com.example.cardwallet.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AppDatabase
import data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private lateinit var database: AppDatabase
    private val cardDao by lazy { database.cardDao() }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _cardCount = MutableStateFlow(0)
    val cardCount: StateFlow<Int> = _cardCount

    fun init(context: Context) {
        database = AppDatabase.getDatabase(context)
    }
    fun loadUser(userId: Long) {
        viewModelScope.launch {
            val userData = database.userDao().getUserById(userId)
            _user.value = userData

            if (userData != null) {
                observeCardCount(userData.userId)
            }
        }
    }

    fun observeCardCount(userId: Long) {
        viewModelScope.launch {
            cardDao.getCardCountForUser(userId).collect { count ->
                _cardCount.value = count
            }
        }
    }
}
