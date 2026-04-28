package com.example.cardwallet

import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    protected fun setupLogoutButton() {
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        logoutBtn?.setOnClickListener {
            performLogout()
        }
    }

    protected fun setupProfileButton() {
        val profile_return_btn = findViewById<Button>(R.id.back_profile)
        profile_return_btn?.setOnClickListener {
            navToProfile()
        }
    }


    fun performLogout() {
        val intent = Intent(this, LogActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun navToProfile(){
            startActivity(Intent(this, ProfileActivity::class.java))
    }

}
