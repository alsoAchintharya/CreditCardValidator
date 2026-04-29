package com.example.cardwallet

import android.content.Intent
import android.content.UriPermission
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import data.AppDatabase
import data.User
import data.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class LogActivity : AppCompatActivity() {

    private lateinit var camView: ImageView

    private var tempUri: Uri? = null
    private var photoFile: File? = null
    private var verified = false

    private lateinit var loginBtn: Button
    private lateinit var verifyBtn: Button

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var currentUsername: String
    private var loggedInUser: User? = null

    private val imglauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                verified = true
                verifyBtn.visibility = View.INVISIBLE
                loginBtn.visibility = View.VISIBLE
                camView.setImageURI(tempUri)

                CoroutineScope(Dispatchers.IO).launch {
                    val user = userDao.getUserByUsername(currentUsername)
                    if (user != null && user.profileImagePath == null) {
                        photoFile?.let {
                            userDao.updateProfileImage(currentUsername, it.absolutePath)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        handleDefaultUser()
        setupUI()
    }

    private fun setupUI() {
        val userField: EditText = findViewById(R.id.user)
        val passwordField: EditText = findViewById(R.id.password)

        loginBtn = findViewById(R.id.login)
        verifyBtn = findViewById(R.id.verButton)
        camView = findViewById(R.id.camcap)

        verifyBtn.setOnClickListener {
            val name = userField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (name.isEmpty() || password.isEmpty()) {
                showToast("Field is required")
            } else {
                currentUsername = name
                CoroutineScope(Dispatchers.IO).launch {
                    val user = userDao.getUserByUsername(name)
                    runOnUiThread {
                        if (user == null) {
                            showToast("User not found")
                        } else if (user.passwordHash != password) {
                            showToast("Incorrect password")
                        } else {
                            loggedInUser = user
                            takePic()
                        }
                    }
                }
            }
        }

        loginBtn.setOnClickListener {
            if (verified && loggedInUser != null) {
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("userId", loggedInUser!!.userId)
                }
                startActivity(intent)
            }
        }
    }


    private fun takePic() {
        photoFile = File.createTempFile("tmp_img_", ".jpg", cacheDir)

        tempUri = FileProvider.getUriForFile(
            this,
            "com.example.cardwallet.fileprovider",
            photoFile!!
        )

        grantUriPermission(
            "com.android.camera",
            tempUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        imglauncher.launch(tempUri!!)
    }

    private fun handleDefaultUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = userDao.getUserByUsername("achintharya")
            if (existing == null) {
                userDao.insert(User(username = "achintharya", passwordHash = "default"))
            }
        }
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}