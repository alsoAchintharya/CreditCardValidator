package com.example.cardwallet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import data.AppDatabase
import data.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : BaseActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    var currentUser: data.User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)


        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        setupLogoutButton()

        val profileUserName = findViewById<TextView>(R.id.profileUser)
        val profileImg = findViewById<ImageView>(R.id.profilePic)
        val cardsButton = findViewById<Button>(R.id.show_cards)

        val userId = intent.getLongExtra("userId", -1L)

        CoroutineScope(Dispatchers.IO).launch {
            val user = userDao.getUserById(userId)
            currentUser = user

            withContext(Dispatchers.Main) {
                profileUserName.text = user?.username?.uppercase() ?: "GUEST"

                user?.profileImagePath?.let { path ->
                    val file = java.io.File(path)
                    if (file.exists()) {
                        profileImg.setImageURI(Uri.fromFile(file))
                    }
                }
            }
        }

        cardsButton.setOnClickListener {
            val showCardsIntent = Intent(this, CardListActivity::class.java)
            showCardsIntent.putExtra("userId", currentUser?.userId ?: -1L)
            showCardsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(showCardsIntent)
        }
    }
}