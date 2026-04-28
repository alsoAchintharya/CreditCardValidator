package com.example.activitynavigation

import android.R
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

    fun performLogout() {
        val intent = Intent(this, LogActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
