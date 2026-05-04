package com.example.cardwallet

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cardwallet.databinding.ActivityProfileBinding
import com.example.cardwallet.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var binding: ActivityProfileBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityBase.logoutBtn.setOnClickListener {
            val intent = Intent(this, LogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        viewModel.init(applicationContext)

        val profileUserName = binding.profileUser
        val userCardCount = binding.userCards
        val profileImg = binding.profilePic
        val cardsButton = binding.showCards

        val userId = intent.getLongExtra("userId", -1L)

        viewModel.loadUser(userId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cardCount.collect { count ->
                    userCardCount.text = "$count"
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { user ->
                    profileUserName.text = user?.username?.uppercase() ?: "GUEST"

                    user?.profileImagePath?.let { path ->
                        val file = java.io.File(path)
                        if (file.exists()) {
                            profileImg.setImageURI(Uri.fromFile(file))
                        }
                    }
                }
            }
        }

        cardsButton.setOnClickListener {
            val user = viewModel.user.value
            if (user != null) {
                val intent = Intent(this, CardListActivity::class.java)
                intent.putExtra("userId", user.userId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "User not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
