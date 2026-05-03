package com.example.cardwallet.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.AppDatabase
import data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogViewModel : ViewModel() {

    private lateinit var database: AppDatabase

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult

    fun init(context: Context) {
        database = AppDatabase.getDatabase(context)
    }

    fun createDefaultUser() {
        viewModelScope.launch {
            val existing = database.userDao().getUserByUsername("achintharya")
            if (existing == null) {
                database.userDao().insert(User(username = "achintharya", passwordHash = "default"))
            }
        }
    }

    fun verifyUser(username: String, password: String) {
        viewModelScope.launch {
            val user = database.userDao().getUserByUsername(username)
            when {
                user == null -> {
                    _loginResult.value = LoginResult.UserNotFound
                }
                user.passwordHash != password -> {
                    _loginResult.value = LoginResult.IncorrectPassword
                }
                else -> {
                    _loggedInUser.value = user
                    _loginResult.value = LoginResult.Success
                }
            }
        }
    }

    fun updateProfileImage(username: String, path: String) {
        viewModelScope.launch {
            database.userDao().updateProfileImage(username, path)
        }
    }

    fun resetLoginResult() {
        _loginResult.value = null
    }

    sealed class LoginResult {
        object Success : LoginResult()
        object UserNotFound : LoginResult()
        object IncorrectPassword : LoginResult()
    }
}
