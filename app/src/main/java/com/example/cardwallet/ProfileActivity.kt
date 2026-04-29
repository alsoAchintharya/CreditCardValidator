package com.example.cardwallet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.cardwallet.databinding.ActivityProfileBinding
import data.AppDatabase
import data.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    private lateinit var binding: ActivityProfileBinding

    var currentUser: data.User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()


        val profileUserName = binding.profileUser
        val profileImg = binding.profilePic
        val cardsButton = binding.showCards

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
            val userId = currentUser?.userId
            if (userId != null) {
                val intent = Intent(this, CardListActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "User not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
    }
}